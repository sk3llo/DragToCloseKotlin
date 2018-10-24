package com.davidmiguel.dragtoclose

import android.content.Context

import android.support.annotation.AttrRes

import android.support.annotation.IdRes

import android.support.v4.view.ViewCompat

import android.support.v4.widget.ViewDragHelper
import android.support.v7.app.AppCompatActivity

import android.util.AttributeSet

import android.view.MotionEvent

import android.view.View

import android.widget.FrameLayout


/**
 *
 * View group that extends FrameLayout and allows to finish an activity dragging down a view.
 *
 */

class DragToClose : FrameLayout {


    // Attributes

    @IdRes
    private var draggableContainerId: Int = 0

    @IdRes
    private var draggableViewId: Int = 0

    @IdRes
    private var fragmentId: Int = 0

    /**
     *
     * Checks whether finish activity is activated or not.
     *
     */

    /**
     *
     * Sets finish activity attribute. If true, the activity is closed when
     *
     * the view is dragged out. Default: true.
     *
     */

    var isFinishActivity: Boolean = false

    private var fragmentTag: String? = ""

    private var closeOnClick: Boolean = false


    private var draggableContainer: View? = null

    private var draggableView: View? = null

    private var draggableContainerTop: Int = 0

    private var draggableContainerLeft: Int = 0


    private var dragHelper: ViewDragHelper? = null

    private var listener: DragListener? = null


    /**
     *
     * Returns the draggable range.
     *
     */

    internal var draggableRange: Int = 0
        private set


    /**
     *
     * Checks whether close on click is activated or not.
     *
     */

    /**
     *
     * Sets close on click attribute. If true, the draggable container is slid down
     *
     * when the draggable view is clicked. Default: false.
     *
     */

    var isCloseOnClick: Boolean
        get() = closeOnClick
        set(closeOnClick) {

            if (closeOnClick) {

                initOnClickListener(draggableView!!)

            } else {

                draggableView!!.setOnClickListener(null)

            }
            this.closeOnClick = closeOnClick

        }


    /**
     *
     * Calculate the dragged view top position normalized between 1 and 0.
     * Used for to change alpha
     */

    private val verticalDragOffset: Float
        get() = Math.abs(draggableContainer!!.top).toFloat() / height.toFloat()


    constructor(context: Context) : super(context) {

    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        initializeAttributes(attrs)

    }


    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        initializeAttributes(attrs)

    }


    /**
     *
     * Configures draggable view and initializes DragViewHelper.
     *
     */

    override fun onFinishInflate() {

        super.onFinishInflate()

        initViews()

        initViewDragHelper()

    }


    /**
     *
     * Gets the height of the DragToClose view and configures the vertical
     *
     * draggable threshold base on it.
     *
     */

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {

        super.onSizeChanged(w, h, oldW, oldH)

        draggableRange = h

    }


    /**
     *
     * Intercepts only touch events over the draggable view.
     *
     */

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        var handled = false

        if (isEnabled) {

            handled = (dragHelper!!.shouldInterceptTouchEvent(event)

//                  Use instead of just
//                  "dragHelper!!.isViewUnder(draggableView, event.x.toInt(), event.y.toInt()))"
//                  to fix animation bug
                    && (dragHelper!!.isViewUnder(draggableView, this.x.toInt(), this.x.toInt())
                    || dragHelper!!.isViewUnder(draggableView, event.x.toInt(), event.y.toInt())))

        } else {

            dragHelper!!.cancel()

        }


        return handled || super.onInterceptTouchEvent(event)

    }


    /**
     *
     * Dispatches touch event to the draggable view.
     *
     * The touch is realized only if is over the draggable view.
     *
     */

    override fun onTouchEvent(event: MotionEvent): Boolean {

        dragHelper?.processTouchEvent(event)

//        Log.e("DHC::::::", "isTouched: ${isViewTouched(draggableView!!, this.x.toInt(), this.y.toInt())}, x: ${event?.x},y: ${event?.y}")

        return isViewTouched(draggableView!!, event.x.toInt(), event.y.toInt())

    }


    /**
     *
     * Automatic settles the draggable view when it is released.
     *
     */

    override fun computeScroll() {

        if (dragHelper!!.continueSettling(true)) {

            ViewCompat.postInvalidateOnAnimation(this)

        }

    }


    /**
     *
     * Returns the draggable view id.
     *
     */

    fun getDraggableViewId(): Int {

        return draggableViewId

    }




    /**
     *
     * Sets the draggable view id.
     *
     */

    fun setDraggableViewId(@IdRes draggableViewId: Int) {

        this.draggableViewId = draggableViewId

        invalidate()

        requestLayout()

    }


    /**
     *
     * Returns the draggable container id.
     *
     */

    fun getDraggableContainerId(): Int {

        return draggableContainerId

    }


    /**
     *
     * Sets the draggable container id.
     *
     */

    fun setDraggableContainerId(@IdRes draggableContainerId: Int) {

        this.draggableContainerId = draggableContainerId

        invalidate()

        requestLayout()

    }


    /**
     *
     * Sets drag listener.
     *
     */

    fun setDragListener(listener: DragListener) {

        this.listener = listener

    }


    /**
     *
     * Slides down draggable container out of the DragToClose view.
     *
     */

    fun closeDraggableContainer() {

        slideViewTo(draggableContainer, paddingLeft + draggableContainerLeft, draggableRange)

    }


    /**
     *
     * Slides up draggable container to its original position.
     *
     */

    fun openDraggableContainer() {

        slideViewTo(draggableContainer, paddingLeft + draggableContainerLeft,

                paddingTop + (draggableContainerTop))

    }


    /**
     *
     * Invoked when the view has just started to be dragged.
     *
     */

    internal fun onStartDraggingView() {

        if (listener != null) {

            listener!!.onStartDraggingView()

        }

    }


    /**
     *
     * Notifies the listener that the view has been closed
     *
     * and finishes the activity (if need be).
     *
     */

    internal fun closeActivity() {

        if (listener != null) {

            listener!!.onViewClosed()

        }

        //Finish fragment by tag
        if (fragmentTag != null && fragmentTag != "") {
            val act = context as AppCompatActivity
            val manager = act.supportFragmentManager

            if (act.supportFragmentManager.findFragmentByTag(fragmentTag) != null) {

                manager.beginTransaction()?.remove(manager.findFragmentByTag(fragmentTag))?.commit()
            }
        }

        //Finish fragment by ID
        if (fragmentId != 0 || fragmentId != -1){
            val act = context as AppCompatActivity
            val manager = act.supportFragmentManager
            val id = act.supportFragmentManager.findFragmentById(fragmentId)

            if (id != null) {
                manager.beginTransaction()?.remove(id)?.commit()
            }
        }

        if (isFinishActivity) {

            val activity = context as AppCompatActivity

            activity.finish()

            activity.overridePendingTransition(0, R.anim.fade_out)

        }

    }


    /**
     *
     * Modify dragged view alpha based on the vertical position while the view is being
     *
     * vertical dragged.
     *
     */

    internal fun changeDragViewViewAlpha() {

        draggableContainer!!.alpha = 1 - verticalDragOffset

    }


    /**
     *
     * Drags the draggable container to given position.
     *
     */

    internal fun smoothScrollToY(settleDestY: Int) {

        if (dragHelper!!.settleCapturedViewAt(paddingLeft, settleDestY)) {

            slideViewTo(draggableContainer, paddingLeft + draggableContainerLeft, settleDestY)
        }
    }


    /**
     *
     * Initializes XML attributes.
     *
     */

    private fun initializeAttributes(attrs: AttributeSet?) {

        val array = context.theme.obtainStyledAttributes(

                attrs, R.styleable.DragToClose, 0, 0)

        try {

            draggableViewId = array.getResourceId(R.styleable.DragToClose_draggableView, -1)

            draggableContainerId = array.getResourceId(R.styleable.DragToClose_draggableContainer, -1)

            isFinishActivity = array.getBoolean(R.styleable.DragToClose_finishActivity, true)

            closeOnClick = array.getBoolean(R.styleable.DragToClose_closeOnClick, false)

            fragmentTag = array.getString(R.styleable.DragToClose_finishFragmentByTag)

            fragmentId = array.getResourceId(R.styleable.DragToClose_finishFragmentByContainerId, -1)

            dragDirection = array.getString(R.styleable.DragToClose_dragDirection)

            if (dragDirection?.isEmpty()!! || dragDirection?.isBlank()!!){

                throw IllegalArgumentException("dragDirection is required.")

            }



            if (draggableViewId == -1 || draggableContainerId == -1) {

                throw IllegalArgumentException("draggableView and draggableContainer attributes are required.")

            }

        } finally {

            array.recycle()

        }

    }


    /**
     *
     * Initializes views.
     *
     */

    private fun initViews() {

        draggableContainer = findViewById(draggableContainerId)

        if (draggableContainer == null) {

            throw IllegalArgumentException("draggableContainer not found!")

        }

        draggableContainerTop = draggableContainer!!.top

        draggableContainerLeft = draggableContainer!!.left

        draggableView = findViewById(draggableViewId)

        if (draggableView == null) {

            throw IllegalArgumentException("draggableView not found!")

        }

        if (closeOnClick) {

            initOnClickListener(draggableView!!)

        }

    }


    /**
     *
     * Initializes on OnClickListener (if need be).
     *
     */

    private fun initOnClickListener(clickableView: View) {

        clickableView.setOnClickListener { closeDraggableContainer() }

    }


    /**
     *
     * Initializes ViewDragHelper.
     *
     */

    private fun initViewDragHelper() {

        dragHelper = ViewDragHelper.create(this, DRAG_SENSITIVITY,

                DragHelperCallback(this, this.draggableContainer!!))

    }


    /**
     *
     * Determines if position (x, y) is below given view.
     *
     */

    private fun isViewTouched(view: View, x: Int, y: Int): Boolean {

        val viewLocation = IntArray(2)

        view.getLocationOnScreen(viewLocation)

        val parentLocation = IntArray(2)

        this.getLocationOnScreen(parentLocation)

        val screenX = parentLocation[0] + x

        val screenY = parentLocation[1] + y

        return (screenX >= viewLocation[0]

                && screenX < viewLocation[0] + view.width

                && screenY >= viewLocation[1]

                && screenY < viewLocation[1] + view.height)

    }


    /**
     *
     * Slides down a view.
     *
     */

    private fun slideViewTo(view: View?, left: Int, top: Int) {

        dragHelper!!.smoothSlideViewTo(view!!, left, top)

        invalidate()

    }

    companion object {


        // Sensitivity detecting the start of a drag (larger values are more sensitive)

        private val DRAG_SENSITIVITY = 1.0f

        // If the view is dragged with a higher speed than the threshold, the view is

        // closed automatically

        internal val SPEED_THRESHOLD_TO_CLOSE_DOWN = 1500.0f
        internal val SPEED_THRESHOLD_TO_CLOSE_UP = -1500.0f

        // If dragging finishes below this threshold the view returns to its original position,

        // if the threshold is exceeded, the view is closed automatically

        val HEIGHT_THRESHOLD_TO_CLOSE = 0.5f

        //Check dragging direction
        var dragDirection: String? = ""

    }

}