package com.comm.library.utils.base

/**
 * @author xqm
 * @date 2025/10/18 11:44
 * @description BaseFragment 类功能说明
 */
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.collections.forEach
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.jvm.javaPrimitiveType
import kotlin.let

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB
    protected lateinit var mContext: Context

    companion object {
        private const val TAG = "BaseFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "${this::class.java.simpleName} created.")
        initView()
        initData()
        initListener()
    }

    /**
     * 初始化 ViewBinding
     */
    @Suppress("UNCHECKED_CAST")
    private fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB {
        return try {
            val superclass: Type = javaClass.genericSuperclass
            if (superclass is ParameterizedType) {
                val clazz = superclass.actualTypeArguments[0] as Class<VB>
                val inflate = clazz.getMethod(
                    "inflate",
                    LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.javaPrimitiveType
                )
                inflate.invoke(null, inflater, container, false) as VB
            } else {
                throw IllegalStateException("Missing generic ViewBinding type")
            }
        } catch (e: Exception) {
            throw RuntimeException("ViewBinding init error: ${e.message}", e)
        }
    }

    /**
     * 子类实现区域
     */
    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initListener()

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "${this::class.java.simpleName} destroyed.")
    }

    /**
     * 批量绑定点击事件
     */
    protected fun setClickListeners(listener: View.OnClickListener, vararg views: View?) {
        views.forEach { it?.setOnClickListener(listener) }
    }

    protected fun goTo(cls: Class<out Activity>, bundle: Bundle? = null, finishCurrent: Boolean = false) {
        val intent = Intent(mContext, cls)
        bundle?.let { intent.putExtras(it) }
        startActivity(intent)
        if (finishCurrent) {
            activity?.finish()
        }
    }
}
