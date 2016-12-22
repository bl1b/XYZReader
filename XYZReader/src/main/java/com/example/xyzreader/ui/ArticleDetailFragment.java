package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    private static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;

    private CollapsingToolbarLayout ctlToolbarLayout;
    private ImageView ivPhoto;
    private TextView tvByline;
    private TextView tvBody;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }


    static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mRootView.findViewById(R.id.articledetail_toolbar_layout) instanceof CollapsingToolbarLayout) {
            ctlToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.articledetail_toolbar_layout);
        }

        if (mRootView.findViewById(R.id.photo) instanceof ImageView) {
            ivPhoto = (ImageView) mRootView.findViewById(R.id.photo);
        }

        if (mRootView.findViewById(R.id.article_byline) instanceof TextView) {
            tvByline = (TextView) mRootView.findViewById(R.id.article_byline);
            tvByline.setMovementMethod(new LinkMovementMethod());
        }

        if (mRootView.findViewById(R.id.article_body) instanceof TextView) {
            tvBody = (TextView) mRootView.findViewById(R.id.article_body);
            tvBody.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        }

        if (mRootView.findViewById(R.id.share_fab) instanceof FloatingActionButton) {
            FloatingActionButton fabShare = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
            fabShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));
                }
            });
        }

        bindDataToViews();
    }

    private void bindDataToViews() {
        if (mRootView != null) {
            mRootView.setAlpha(0);
            mRootView.animate().alpha(1);
            mRootView.setVisibility(View.VISIBLE);
        } else {
            throw new RuntimeException("Binding without root-view.");
        }

        if (mCursor != null) {
            if (ctlToolbarLayout != null) {
                ctlToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            }

            if (tvByline != null) {
                tvByline.setText(getBylineText());
            }

            if (tvBody != null) {
                tvBody.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            }

            if (ivPhoto != null) {
                ImageLoaderHelper.getInstance(getActivity()).getImageLoader().get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = new Palette.Builder(bitmap).maximumColorCount(12).generate();

                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            ivPhoto.setImageBitmap(imageContainer.getBitmap());
                            if (ctlToolbarLayout != null) {
                                ctlToolbarLayout.setContentScrimColor(mMutedColor);
                            }
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //NOOP
                    }
                });
            }
        } else {
            if (mRootView != null) {
                mRootView.setVisibility(View.GONE);
            }
        }
    }

    private CharSequence getBylineText() {
        if (mCursor != null) {
            String bylineText = DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();

            bylineText += " by " + mCursor.getString(ArticleLoader.Query.AUTHOR);
            return bylineText;
        }
        return "";
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindDataToViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindDataToViews();
    }
}
