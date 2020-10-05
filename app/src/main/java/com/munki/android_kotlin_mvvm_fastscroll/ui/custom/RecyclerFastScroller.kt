package com.munki.android_kotlin_mvvm_fastscroll.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.SectionIndexer
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munki.android_kotlin_mvvm_fastscroll.R
import com.munki.android_kotlin_mvvm_fastscroll.databinding.ItemFastScrollerBinding
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * RecyclerFastScroller
 * @author 나비이쁜이
 * @since 2020.10.05
 */
class RecyclerFastScroller : LinearLayout {

    // Context & Binding & ScrollListener
    private var mContext: Context
    private var mBinding: ItemFastScrollerBinding? = null
    private var onFastScrollListener: OnFastScrollListener? = null

    // Recyclerview & Height & ObjectAnimator
    private var scrollerHeight = 0
    private var recyclerView: RecyclerView? = null
    private var currentAnimator: ObjectAnimator? = null
    private var bubbleAnimator: ObjectAnimator? = null

    // Setting Options
    private var handleRadius = 0f
    private var handleMargin = 0
    private var handleWidth = 0

    // Canvas RectF -> Float 값을 지정하는 그래픽s 클래스
    private var handlePositionRect: RectF? = null

    // Canvas Paint -> 그리기 도구
    private var handleBackgroundPaint: Paint? = null
    private var handleTextPaint: Paint? = null

    // Static
    companion object {
        // Word Sections
        private lateinit var bubbleList: Array<String?>
        private var HandleMap: LinkedHashMap<String, Int?>? = null

        // Bubble Option
        private const val BUBBLE_ANIMATION_DURATION: Long = 500
        private const val TRACK_SNAP_RANGE = 5
    }

    // - 생성자
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        init(attrs)
    }

    // - 생성자
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        init(attrs)
    }

    // - Init
    private fun init(attrs: AttributeSet?) {
        // View - DataBinding
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_fast_scroller, this, true)

        // 가로 방향 설정 & 범위 설정 OFF
        this.orientation = HORIZONTAL
        this.clipChildren = false

        // init
        onFastScrollListener = OnFastScrollListener()
        bubbleList = arrayOf()
        HandleMap = LinkedHashMap()

        // RectF init
        handlePositionRect = RectF()

        // Paint init
        handleTextPaint = Paint()
        handleBackgroundPaint = Paint()

        // Ant Alias -> 안티얼레이징 -> 색상차가 뚜렷한 경계 부근에 중간색을 삽입하여 도형이나 글꼴이 주변 배경과 부드럽게 잘 어울리도록 하는 기법 (feat.게임)
        handleBackgroundPaint!!.isAntiAlias = true

        // Values - Attrs (Xml Option)
        val array = mContext.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FastScrollSection,
            0,
            0
        )

        // Values - Attrs value - Default Value (지정하지 않은다면 기본값 설정)
        val handleBackgroundColor = array.getColor(
            R.styleable.FastScrollSection_handleBackgroundColor,
            -0x1
        )
        val handleTextColor = array.getColor(
            R.styleable.FastScrollSection_handleTextColor,
            -0x1000000
        )
        val handleRadius = array.getFloat(R.styleable.FastScrollSection_handleRadius, 60f)
        val handleWidth = array.getInt(R.styleable.FastScrollSection_handleWidth, 20)
        val handleMargin = array.getInt(R.styleable.FastScrollSection_handleMargin, 0)

        // Setter - Handle
        setHandleBackgroundColor(handleBackgroundColor)
        setHandleTextColor(handleTextColor)
        setHandleRadius(handleRadius)
        setHandleWidth(handleWidth)
        setHandleMargin(handleMargin)

        // recycle -> Remove | 초기 화면에 Alpha값을 초기화 하기 위해서 0으로 지정
        this.alpha = 0f
        array.recycle()
    }

    /************************************************************************************************************************************************/

    // Override

    /**
     * onSizeChanged
     */
    override
    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        scrollerHeight = height
    }

    /**
     * onDetachedFromWindow
     */
    override
    fun onDetachedFromWindow() {
        recyclerView!!.removeOnScrollListener(onFastScrollListener!!)
        super.onDetachedFromWindow()
    }

    /**
     * onTouchEvent
     */
    @SuppressLint("ClickableViewAccessibility")
    override
    fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                showHandle(true)

                if (event.x < mBinding!!.ivHandle.x - ViewCompat.getPaddingStart(mBinding!!.ivHandle)) return false

                if (currentAnimator != null) currentAnimator!!.cancel()

                if (mBinding!!.tvBubble.visibility == GONE) showBubble(true)

                mBinding!!.ivHandle.isSelected = true

                val y = event.y
                setFastScrollerPosition(y)
                setRecyclerViewPosition(y)
                true
            }

            MotionEvent.ACTION_MOVE -> {
                showHandle(true)

                val y = event.y
                setFastScrollerPosition(y)
                setRecyclerViewPosition(y)
                true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                showHandle(false)

                mBinding!!.ivHandle.isSelected = false

                showBubble(false)
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    /**
     * dispatchDraw
     */
    override
    fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // section이 없는 경우 return
        if (bubbleList.isEmpty()) return

        val handleWidthDensity = handleWidth * density
        val handleWidthDensityZIP = this.handleWidth * density
        val handleMarginDensity = handleMargin * density

        // 좌측 공간 -> Handle 공간을 제외한 Bubble이 표현될 모든 공간
        val leftBubblePosition = this.width - this.paddingRight - handleWidthDensity

        // Handle 사각형 범위 (Radius 제외)
        handlePositionRect!!.left = leftBubblePosition
        handlePositionRect!!.right = leftBubblePosition + handleWidthDensity
        handlePositionRect!!.top = this.paddingTop.toFloat()
        handlePositionRect!!.bottom = this.height - this.paddingBottom.toFloat()

        // Handle Draw -> Rect / x축 radius / y축 radius / Paint
        canvas.drawRoundRect(
            handlePositionRect!!,
            handleRadius,
            handleRadius,
            handleBackgroundPaint!!
        )

        // 상하 패딩을 제외한 값에 bubble 리스트 크기만큼 나눈 사이즈
        val handleSize = (this.height - this.paddingTop - paddingBottom) / bubbleList.size - 1

        // Handle Text Size
        handleTextPaint!!.textSize = handleWidthDensity / 2

        // Handle Text Draw
        for (i in bubbleList.indices) {
            val x = (leftBubblePosition + handleTextPaint!!.textSize / 1.5).toFloat()
            val y =
                if (this.height - (handleWidthDensityZIP + handleSize * i) > 100)
                    handleWidthDensityZIP + paddingTop + handleMarginDensity + handleSize * i
                else handleWidthDensityZIP + paddingTop + handleSize * i

            canvas.drawText(
                bubbleList[i]!!.toUpperCase(Locale.getDefault()),
                x,
                y,
                handleTextPaint!!
            )
        }
    }

    /************************************************************************************************************************************************/

    // Public Method

    /**
     * setRecyclerView
     */
    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        this.recyclerView!!.addOnScrollListener(onFastScrollListener!!)
    }

    /**
     * setKeywordList
     */
    fun setKeywordList(keywordList: ArrayList<String>) {
        Collections.sort(keywordList, OrderingByKorean.comparator)
        for (i in keywordList.indices) {
            val item = keywordList[i]
            var index = item.substring(0, 1)
            val c = index[0]

            if (OrderingByKorean.isKorean(c))
                index = KoreanChar.getCompatChoseong(c).toString()

            if (HandleMap!![index] == null)
                HandleMap!![index] = i
        }

        val indexList = ArrayList(HandleMap!!.keys)
        bubbleList = arrayOfNulls(indexList.size)

        indexList.toArray(bubbleList)
        indexList.clear()
        indexList.trimToSize()
    }

    /**
     * showHandle
     */
    private fun showHandle(show: Boolean) {
        if (show) {
            if (bubbleAnimator != null)
                bubbleAnimator!!.cancel()

            this.alpha = 1f
        } else {
            bubbleAnimator = ObjectAnimator.ofFloat(this, ALPHA, 1f, 0f).setDuration(
                BUBBLE_ANIMATION_DURATION
            )

            if (this.alpha == 1f)
                Handler().postDelayed({ bubbleAnimator!!.start() }, 1000)
        }
    }

    /************************************************************************************************************************************************/

    // XML Public Method

    /**
     * Handle Background Color Setting
     */
    private fun setHandleBackgroundColor(colorInt: Int) {
        handleBackgroundPaint!!.color = colorInt
    }

    /**
     * Handle TexrtColor Setting
     */
    private fun setHandleTextColor(colorInt: Int) {
        handleTextPaint!!.color = colorInt
    }

    /**
     * Handle Background Radius Setting
     */
    private fun setHandleRadius(radiusInt: Float) {
        handleRadius = radiusInt
    }

    /**
     * Handle Width
     */
    private fun setHandleWidth(widthInt: Int) {
        handleWidth = widthInt
    }

    /**
     * Handle TOP-BOTTOM Margin Setting
     */
    private fun setHandleMargin(marginInt: Int) {
        handleMargin = marginInt
    }

    /************************************************************************************************************************************************/

    // private Method

    /**
     * showBubble
     * true     -> Bubble Show
     * false    -> Bubble Not Show
     */
    private fun showBubble(show: Boolean) {
        if (show) {
            mBinding!!.tvBubble.visibility = VISIBLE
            if (currentAnimator != null) currentAnimator!!.cancel()
            currentAnimator =
                ObjectAnimator.ofFloat(mBinding!!.tvBubble, "alpha", 0f, 1f).setDuration(
                    BUBBLE_ANIMATION_DURATION
                )
        } else {
            if (currentAnimator != null) currentAnimator!!.cancel()
            currentAnimator =
                ObjectAnimator.ofFloat(mBinding!!.tvBubble, "alpha", 1f, 0f).setDuration(
                    BUBBLE_ANIMATION_DURATION
                )
            currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    bubbleHide()
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    bubbleHide()
                }

                private fun bubbleHide() {
                    mBinding!!.tvBubble.visibility = GONE
                    currentAnimator = null
                }
            })
        }
        currentAnimator!!.start()
    }

    /**
     * updateFastScrollerPosition
     */
    private fun updateFastScrollerPosition() {
        if (mBinding!!.ivHandle.isSelected)
            return

        val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView!!.computeVerticalScrollRange()
        val proportion = verticalScrollOffset.toFloat() / verticalScrollRange.toFloat()
        setFastScrollerPosition(scrollerHeight.toFloat() * proportion)
    }

    /**
     * setFastScrollerPosition
     */
    private fun setFastScrollerPosition(y: Float) {
        val handleHeight = mBinding!!.ivHandle.height

        mBinding!!.ivHandle.y = getValueInRange(
            0,
            scrollerHeight - handleHeight,
            (y - (handleHeight / 2).toFloat()).toInt()
        )
    }

    /**
     * setRecyclerViewPosition
     */
    private fun setRecyclerViewPosition(y: Float) {
        val itemCount = recyclerView!!.adapter!!.itemCount

        val proportion = when {
            mBinding!!.ivHandle.y == 0f -> 0f
            mBinding!!.ivHandle.y + mBinding!!.ivHandle.height >= scrollerHeight - TRACK_SNAP_RANGE -> 1f
            else -> y / scrollerHeight.toFloat()
        }

        // to Recyclerview Move Position
        val targetPosition = getValueInRange(
            0,
            itemCount - 1,
            (proportion * itemCount).toInt()
        ).toInt()

        (recyclerView!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
            targetPosition,
            0
        )

        // Bubble setText
        val bubbleText = (recyclerView!!.adapter as FastScrollable?)!!.setBubbleText(targetPosition)
        mBinding!!.tvBubble.text = bubbleText
    }

    /**
     * getValueInRange
     */
    private fun getValueInRange(min: Int, max: Int, adjust: Int): Float {
        val minimum = min.coerceAtLeast(adjust)
        return minimum.coerceAtMost(max).toFloat()
    }

    /**
     * getDensity
     */
    private val density: Float get() = mContext.resources.displayMetrics.density

    /************************************************************************************************************************************************/

    // Adapter, Listener, Interfaces

    // - KoreanIndexerRecyclerAdapter
    abstract class KoreanIndexerRecyclerAdapter<T : RecyclerView.ViewHolder?> : RecyclerView.Adapter<T>(), SectionIndexer {
        override
        fun getSections(): Array<String?> {
            return bubbleList
        }

        override
        fun getPositionForSection(section: Int): Int {
            return HandleMap!![bubbleList[section]]!!
        }

        override
        fun getSectionForPosition(position: Int): Int {
            return 0
        }
    }

    /**
     * OnFastScrollListener
     */
    inner class OnFastScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            updateFastScrollerPosition()
        }
    }

    /**
     * FastScrollable
     */
    interface FastScrollable {
        fun setBubbleText(position: Int): String?
    }

    /************************************************************************************************************************************************/

    // Korean Ordering
    // - Korean - https://github.com/bangjunyoung/KoreanTextMatcher
    private object KoreanChar {
        private const val CHOSEONG_COUNT = 19
        private const val JUNGSEONG_COUNT = 21
        private const val JONGSEONG_COUNT = 28
        private const val HANGUL_SYLLABLE_COUNT = CHOSEONG_COUNT * JUNGSEONG_COUNT * JONGSEONG_COUNT
        private const val HANGUL_SYLLABLES_BASE = 0xAC00
        private const val HANGUL_SYLLABLES_END = HANGUL_SYLLABLES_BASE + HANGUL_SYLLABLE_COUNT
        private val COMPAT_CHOSEONG_MAP = intArrayOf(
            0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
            0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
        )

        private fun isSyllable(c: Char): Boolean {
            return c.toInt() in HANGUL_SYLLABLES_BASE until HANGUL_SYLLABLES_END
        }

        fun getCompatChoseong(value: Char): Char {
            if (!isSyllable(value)) return '\u0000'
            val choseongIndex = getChoseongIndex(value)
            return COMPAT_CHOSEONG_MAP[choseongIndex].toChar()
        }

        private fun getChoseongIndex(syllable: Char): Int {
            val syllableIndex = syllable.toInt() - HANGUL_SYLLABLES_BASE
            return syllableIndex / (JUNGSEONG_COUNT * JONGSEONG_COUNT)
        }
    }

    // - Order - http://reimaginer.tistory.com/entry/한글영어특수문자-순-정렬하는-java-compare-메서드-만들기
    private object OrderingByKorean {
        private const val REVERSE = -1
        private const val LEFT_FIRST = -1
        private const val RIGHT_FIRST = 1

        val comparator: Comparator<String>
            get() = Comparator { left, right ->
                compare(
                    left,
                    right
                )
            }

        private fun compare(left: String, right: String): Int {
            var leftU = left
            var rightU = right
            leftU = StringUtils.upperCase(leftU).replace(" ".toRegex(), "")
            rightU = StringUtils.upperCase(rightU).replace(" ".toRegex(), "")

            val leftLen = leftU.length
            val rightLen = rightU.length
            val minLen = leftLen.coerceAtMost(rightLen)

            for (i in 0 until minLen) {
                val leftChar = leftU[i]
                val rightChar = rightU[i]
                if (leftChar != rightChar) {
                    return if (isKoreanAndEnglish(
                            leftChar,
                            rightChar
                        ) || isKoreanAndNumber(
                            leftChar,
                            rightChar
                        )
                        || isEnglishAndNumber(
                            leftChar,
                            rightChar
                        ) || isKoreanAndSpecial(
                            leftChar,
                            rightChar
                        )
                    ) {
                        (leftChar - rightChar) * REVERSE
                    } else if (isEnglishAndSpecial(
                            leftChar,
                            rightChar
                        ) || isNumberAndSpecial(
                            leftChar,
                            rightChar
                        )
                    ) {
                        if (isEnglish(
                                leftChar
                            ) || isNumber(
                                leftChar
                            )
                        ) {
                            LEFT_FIRST
                        } else {
                            RIGHT_FIRST
                        }
                    } else {
                        leftChar - rightChar
                    }
                }
            }
            return leftLen - rightLen
        }

        private fun isKoreanAndEnglish(ch1: Char, ch2: Char): Boolean {
            return isEnglish(ch1) && isKorean(ch2) || isKorean(ch1) && isEnglish(ch2)
        }

        private fun isKoreanAndNumber(ch1: Char, ch2: Char): Boolean {
            return isNumber(ch1) && isKorean(ch2) || isKorean(ch1) && isNumber(ch2)
        }

        private fun isEnglishAndNumber(ch1: Char, ch2: Char): Boolean {
            return isNumber(ch1) && isEnglish(ch2) || isEnglish(ch1) && isNumber(ch2)
        }

        private fun isKoreanAndSpecial(ch1: Char, ch2: Char): Boolean {
            return isKorean(ch1) && isSpecial(ch2) || isSpecial(ch1) && isKorean(ch2)
        }

        private fun isEnglishAndSpecial(ch1: Char, ch2: Char): Boolean {
            return isEnglish(ch1) && isSpecial(ch2) || isSpecial(ch1) && isEnglish(ch2)
        }

        private fun isNumberAndSpecial(ch1: Char, ch2: Char): Boolean {
            return isNumber(ch1) && isSpecial(ch2) || isSpecial(ch1) && isNumber(ch2)
        }

        private fun isEnglish(ch: Char): Boolean {
            return ch.toInt() >= 'A'.toInt() && ch.toInt() <= 'Z'.toInt() || ch.toInt() >= 'a'.toInt() && ch.toInt() <= 'z'.toInt()
        }

        fun isKorean(ch: Char): Boolean {
            return ch.toInt() >= "AC00".toInt(16) && ch.toInt() <= "D7A3".toInt(16)
        }

        private fun isNumber(ch: Char): Boolean {
            return ch.toInt() >= '0'.toInt() && ch.toInt() <= '9'.toInt()
        }

        private fun isSpecial(ch: Char): Boolean {
            return (ch.toInt() >= '!'.toInt() && ch.toInt() <= '/'.toInt() // !"#$%&'()*+,-./
                    || ch.toInt() >= ':'.toInt() && ch.toInt() <= '@'.toInt() // :;<=>?@
                    || ch.toInt() >= '['.toInt() && ch.toInt() <= '`'.toInt() // [\]^_`
                    || ch.toInt() >= '{'.toInt() && ch.toInt() <= '~'.toInt()) // {|}~
        }
    }
}