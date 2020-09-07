package com.rk.commonlib.widge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rk.commonlib.R;

import java.util.ArrayList;
import java.util.List;


public class IpEditWidge extends LinearLayout implements TextWatcher {
    private static final String TAG = IpEditWidge.class.getSimpleName();


    private int width;
    private int height;
    private Paint paint;

    private static final int DEFAULT_TEXT_MAX_LENGTH = 3;
    private static final int DEFAULT_TEXT_SIZE = 16;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_BORDER_COLOR = Color.argb(60, 255, 0, 0);
    private static final int DEFAULT_BORDER_WIDTH = 2;
    private static final int DEFAULT_POINT_COLOR = Color.BLACK;
    private static final int DEFAULT_POINT_WIDTH = 2;
    private static final int DEFAULT_IP_EDITTEXT_LENGTH = 4;

    private int textLength;
    private int textSize;
    private int textColor;

    private int borderColor;
    private int borderWidth;

    private int pointColor;
    private int pointWidth;
    private int editNumber;


    private int default_height = px2dp(20);
    private int default_width = px2dp(40);

    private List<EditText> data = new ArrayList<>();

    public IpEditWidge(Context context) {
        this(context, null);
    }

    public IpEditWidge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IpEditWidge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IPEditText, defStyleAttr, 0);
        textLength = ta.getInt(R.styleable.IPEditText_textLength, DEFAULT_TEXT_MAX_LENGTH);

        textSize = (int) ta.getDimension(R.styleable.IPEditText_IPtextSize, DEFAULT_TEXT_SIZE);
        textColor = ta.getColor(R.styleable.IPEditText_IPtextColor, DEFAULT_TEXT_COLOR);

        borderColor = ta.getColor(R.styleable.IPEditText_borderColor, DEFAULT_BORDER_COLOR);
        borderWidth = (int) ta.getDimension(R.styleable.IPEditText_borderWidth, DEFAULT_BORDER_WIDTH);

        pointColor = ta.getColor(R.styleable.IPEditText_pointColor, DEFAULT_POINT_COLOR);
        pointWidth = (int) ta.getDimension(R.styleable.IPEditText_pointWidth, DEFAULT_POINT_WIDTH);

        editNumber = ta.getInt(R.styleable.IPEditText_editNumber, DEFAULT_IP_EDITTEXT_LENGTH);

        init(context);
        initPaint();
    }

    public float getTextSize() {
        return data.get(0).getTextSize();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
    }

    private void init(Context context) {
        for (int i = 0; i < editNumber; i++) {
            EditText edit = new EditText(context);
            edit.setBackgroundColor(getResources().getColor(R.color.gray_bg));
            edit.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255", "3")});
            edit.setTextSize(textSize);
            edit.setTextColor(textColor);
            edit.setGravity(Gravity.CENTER);
            edit.setPadding(0,0,0,0);
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
            edit.setMinHeight(default_height);
            edit.setMinWidth(default_width);
            edit.setTag(i);
            edit.setMaxLines(1);
            edit.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
            edit.addTextChangedListener(this);
            edit.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mEditText = (EditText) v;
                    return false;
                }
            });

            edit.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL & event.getAction() == KeyEvent.ACTION_DOWN) {

                        //doSomething();

                        int a = data.indexOf(mEditText);
                        if (a > 0 & mEditText.getSelectionStart() == 0) {
                            data.get(a - 1).requestFocus();
                            mEditText = data.get(a - 1);
                            mEditText.setSelection(mEditText.length());
                        }

                    }

                    return false;
                }

            });
            addView(edit);
            if (i < editNumber - 1) {
                TextView divider = new TextView(context);
                divider.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0));
                divider.setText(".");
                divider.setGravity(Gravity.BOTTOM);
                addView(divider);
            }
            data.add(edit);
        }

        setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));
        setOrientation(LinearLayout.HORIZONTAL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int l = data.get(0).getLeft();
        int t = data.get(0).getTop() - getPaddingTop();
        int r = width - getPaddingRight();
        int b = height;

        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, height - 5, width, height - 5, paint);

        int y = height - height / 5;
        int x = width / editNumber;
        paint.setStrokeWidth(pointWidth);
        paint.setColor(pointColor);
        for (int i = 1; i < data.size(); i++) {
            canvas.drawPoint(x * i, y, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    public EditText mEditText;


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (listener != null) {
            listener.afterTextChanged(getSuperEditTextValue());
        }
        if (s.length() == 3) {
            int a = data.indexOf(mEditText);
            if (a < 3) {
                data.get(a + 1).requestFocus();
                mEditText = data.get(a + 1);
            }

        } else if (s.length() == 2) {
            int a = data.indexOf(mEditText);
            if (a < 3 & (Integer.parseInt(s.toString()) > 25)) {
                data.get(a + 1).requestFocus();
                mEditText = data.get(a + 1);
            }
        }
    }

    public String[] getSuperEditTextValue() {
        String[] val = new String[editNumber];
        for (int i = 0; i < editNumber; i++) {
            val[i] = data.get(i).getText().toString();
            // KLog.d(data.get(i).getText().toString());
        }
        return val;
    }

    public String getIp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < editNumber; i++) {
            String ip_item = data.get(i).getText().toString();
            if (TextUtils.isEmpty(ip_item)) {
                ip_item = "0";
            }
            sb.append(ip_item);
            if (i != editNumber - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public void setIp(String ip) {
        if (ip == null || ip.split("\\.") == null) {
            return;
        }

        String[] ip_array = ip.split("\\.");
        if (ip_array.length < editNumber) {
            return;
        }
        for (int i = 0; i < editNumber; i++) {
            data.get(i).setText(ip_array[i]);
        }
    }

    public void setSuperEdittextValue(String[] s) {
        for (int i = 0; i < s.length; i++) {
            data.get(i).setText(s[i]);
        }
    }

    public boolean getSuperCompile() {
        for (int i = 0; i < editNumber; i++) {
            String str = data.get(i).getText().toString();
            if (Integer.parseInt(str) <= 255) {
                return true;
            }
        }
        return false;
    }

    public int px2dp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    public int px2sp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, val, getResources().getDisplayMetrics());
    }


    public interface SuperTextWatcher {
        public void afterTextChanged(String[] s);
    }

    private SuperTextWatcher listener;

    public void setSuperTextWatcher(SuperTextWatcher listener) {
        this.listener = listener;
    }


    public class InputFilterMinMax implements InputFilter {

        private Double min, max;
        private int maxCount;

        public InputFilterMinMax(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max, String maxCount) {
            this.min = Double.parseDouble(min);
            this.max = Double.parseDouble(max);
            this.maxCount = Integer.parseInt(maxCount);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {

                if (source.equals("-") & dest.toString().equals("")) {
                    return "-";
                } else if (!dest.toString().contains("-") & source.toString().equals("-")) {
                    String strInut = source.toString() + dest.toString();
                    if (strInut.replace(".", "").replace("-", "").length() <= maxCount) {
                        Double input = Double.parseDouble(strInut);
                        if (isInRange(min, max, input))
                            return null;
                    } else {
                        return "";
                    }
                } else {
                    String strInut = dest.toString() + source.toString();
                    if (strInut.replace(".", "").replace("-", "").length() <= maxCount) {
                        Double input = Double.parseDouble(strInut);
                        if (isInRange(min, max, input))
                            return null;
                    } else {
                        return "";
                    }
                }
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(Double a, Double b, Double c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}
