package com.github.mem;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.github.mem.EmotionUtils.EMOTION_CLASSIC_TYPE;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ViewGroup.LayoutParams mLayoutParams;
    private InputMethodManager mInputManager;
    EditText mEditText;
    List<GridView> list = new ArrayList<>();
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mViewPager = findViewById(R.id.viewpager);
        mEditText = findViewById(R.id.edit);
        mTextView = findViewById(R.id.textView);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = mEditText.getText().toString();
                mTextView.setText(SpanStringUtils.getEmotionContent(EMOTION_CLASSIC_TYPE, MainActivity.this, mTextView, string));
                mEditText.setText("");
            }
        });

        EmotionInputDetector.with(this)
                .setEmotionView(mViewPager)
                .bindToContent(findViewById(R.id.textView))
                .bindToEditText((EditText) findViewById(R.id.edit))
                .bindToEmotionButton(findViewById(R.id.iv_em))
                .build();

        List<String> emList = new ArrayList<>();
        for (String em : EmotionUtils.getEmojiMap(EMOTION_CLASSIC_TYPE).keySet()) {
            emList.add(em);
            if (emList.size() == 20) {
                GridView gridView = createGridView(emList);
                list.add(gridView);
                emList = new ArrayList<>();
            }
        }

        if (emList.size() > 0) {
            GridView gridView = createGridView(emList);
            list.add(gridView);
        }
        mViewPager.setAdapter(new ViewPagerAdapte(list));
    }

    private GridView createGridView(List<String> list) {
        int screenWidthPixels = DisplayUtils.getScreenWidthPixels(this);
        int screenHeightPixels = DisplayUtils.getScreenHeightPixels(this);
        int pading = DisplayUtils.dp2px(this, 12);
        int itemWidth = (screenWidthPixels - pading * 8) / 7;
        int itemHeight = itemWidth * 3 + pading * 6;
        GridView gridView = new GridView(this);
        gridView.setSelector(android.R.color.transparent);
        //设置7列
        gridView.setNumColumns(7);
        gridView.setPadding(pading, pading, pading, pading);
        gridView.setHorizontalSpacing(pading);
        gridView.setVerticalSpacing(pading * 2);
        //设置GridView的宽高
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(screenWidthPixels, itemHeight);
        gridView.setLayoutParams(params);
        gridView.setAdapter(new GridViewAdapter(itemWidth, list));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GridViewAdapter adapterView1 = (GridViewAdapter) adapterView.getAdapter();
                if (i == adapterView1.getCount() - 1) {
                    // 如果点击了最后一个回退按钮,则调用删除键事件
                    mEditText.dispatchKeyEvent(new KeyEvent(
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }else{
                    // 如果点击了表情,则添加到输入框中
                    String emotionName = (String) adapterView1.getItem(i);
                    // 获取当前光标位置,在指定位置上添加表情图片文本
                    int curPosition = mEditText.getSelectionStart();
                    StringBuilder sb = new StringBuilder(mEditText.getText().toString());
                    sb.insert(curPosition, emotionName);

                    // 特殊文字处理,将表情等转换一下
                    mEditText.setText(SpanStringUtils.getEmotionContent(EMOTION_CLASSIC_TYPE,
                            MainActivity.this, mEditText, sb.toString()));

                    // 将光标设置到新增完表情的右侧
                    mEditText.setSelection(curPosition + emotionName.length());
                }
            }
        });
        return gridView;
    }


    class GridViewAdapter extends BaseAdapter{

        private int itemWidth;
        private List<String> mList;

        public GridViewAdapter(int itemWidth, List<String> list) {
            this.itemWidth = itemWidth;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size() + 1;
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ImageView imageView = new ImageView(MainActivity.this);
            // 设置内边距
            imageView.setPadding(itemWidth/8, itemWidth/8, itemWidth/8, itemWidth/8);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(itemWidth, itemWidth);
            imageView.setLayoutParams(params);
            //判断是否为最后一个item
            if(position == getCount() - 1) {
                imageView.setImageResource(R.drawable.compose_emotion_delete);
            } else {
                String emotionName = mList.get(position);
                imageView.setImageResource(EmotionUtils.getImgByName(EmotionUtils.EMOTION_CLASSIC_TYPE,emotionName));
            }
            return imageView;
        }
    }


    class ViewPagerAdapte extends PagerAdapter{

        private List<GridView> gvs;

        public ViewPagerAdapte(List<GridView> gvs) {
            this.gvs = gvs;
        }

        @Override
        public int getCount() {
            return gvs.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(gvs.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(gvs.get(position));
            return gvs.get(position);
        }
    }
}
