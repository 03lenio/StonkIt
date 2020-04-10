package de.liquid.stonkit.stickerview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.liquid.stonkit.R;
import de.liquid.stonkit.SelectImageActivity;


public class EditActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageView imageStonks;
    private FrameLayout canvas;
    private Button crop;
    private Button text;
    private Button stonks;
    private Button captionBtn;
    private Button captionOffBtn;
    private Button back;
    private Button share;
    private Button save;
    private StickerImageView iv_sticker;
    private StickerTextView tv_sticker;

    private String m_Text = "";

    private Boolean iv_stickerInUse = false;
    private Boolean tv_stickerInUse = false;
    private Boolean share_requested = true;

    private InterstitialAd mInterstitialAd;

    private View caption;

    private int originalHeight;
    private int realHeight;
    private int captionSizeID;

    private TextView captionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        imageView = findViewById(R.id.editPresetEdit);
        imageStonks = findViewById(R.id.stonks);
        canvas = findViewById(R.id.canvas);
        crop = findViewById(R.id.cropBtn);
        text = findViewById(R.id.textStickerBtn);
        stonks = findViewById(R.id.stonksBtn);
        captionBtn = findViewById(R.id.captionBtn);
        captionOffBtn = findViewById(R.id.captionOffBtn);
        back = findViewById(R.id.backBtn);
        share = findViewById(R.id.shareBtn);
        save = findViewById(R.id.saveBtn);
        imageView.setRotation(SelectImageActivity.ROTATION);
        imageView.setImageBitmap(SelectImageActivity.publicBitMap);
        caption = findViewById(R.id.caption);
        originalHeight = canvas.getHeight();


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6347439572384089/7337460931");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                if (share_requested) {
                    shareImage();
                    share_requested = false;
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                } else {
                    saveImage();
                    share.requestFocus();
                    // Load the next interstitial.
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }


            }

        });


        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(SelectImageActivity.contentURIOut);
                CropImage.activity(SelectImageActivity.contentURIOut).start(EditActivity.this);
            }
        });

        stonks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showStonksDialog();

            }
        });

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTextDialog();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    share_requested = false;
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                    catchAdFailedToLoad();
                }

            }


        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    share_requested = true;
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                    catchAdFailedToLoad();
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToSelect();

            }
        });

        captionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptionDialog();
            }
        });

        captionOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCaption();
            }
        });

        caption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCaptionTextDialog();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                crop.setText(R.string.re_crop);
                //      imageView.getLayoutParams().height = SelectImageActivity.finalHeight;
                //      imageView.getLayoutParams().width = SelectImageActivity.finalWidth;
                imageView.setRotation(SelectImageActivity.ROTATION);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void inputTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_text);


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tv_stickerInUse) {
                    tv_sticker.setControlItemsHidden(true);
                }
                m_Text = input.getText().toString();
                tv_sticker = new StickerTextView(getApplicationContext());
                tv_sticker.setText(m_Text);
                canvas.addView(tv_sticker);
                tv_stickerInUse = true;
                canvas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        for (int i = 0; i < canvas.getChildCount(); i++) {

                            View child = canvas.getChildAt(i);
                            if (child instanceof StickerImageView) {

                                ((StickerImageView) child).setControlItemsHidden(true);
                            }

                            if (child instanceof StickerTextView) {

                                ((StickerTextView) child).setControlItemsHidden(true);
                            }
                        }

                    }
                });

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    @Override
    public void onBackPressed() {
        System.out.println("na");
    }

    private void catchAdFailedToLoad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ad_no_internet)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void returnToSelect() {

        SelectImageActivity.ROTATION = 0;
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);

    }


    private void showStonksDialog() {
        final AlertDialog.Builder stonksDialog = new AlertDialog.Builder(this);
        stonksDialog.setTitle(R.string.stonks_size);
        String[] pictureDialogItems = {
                getString(R.string.xtrasmall),
                getString(R.string.small),
                "Medium",
                "Medium+",
                getString(R.string.large),
                "Thicc"};
        stonksDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                sizeExtraSmall();
                                break;
                            case 1:
                                sizeSmall();
                                break;
                            case 2:
                                sizeMedium();
                                break;
                            case 3:
                                sizeMediumPlus();
                                break;
                            case 4:
                                sizeLarge();
                                break;
                            case 5:
                                sizeThicc();
                                break;
                        }
                    }
                });
        stonksDialog.show();
    }

    private void sizeExtraSmall() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks_lil);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }

            }
        });


    }

    private void sizeSmall() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks_small);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }

            }
        });

    }

    private void sizeMedium() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks_best);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }

            }
        });

    }

    private void sizeMediumPlus() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks_medium);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }

            }
        });

    }

    private void sizeLarge() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks_large);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }

            }
        });

    }

    private void sizeThicc() {
        if (iv_stickerInUse) {
            iv_sticker.setControlItemsHidden(true);
        }
        imageStonks.setImageResource(R.drawable.stonks);
        iv_sticker = new StickerImageView(getApplicationContext());
        iv_sticker.setImageDrawable(imageStonks.getDrawable());
        canvas.addView(iv_sticker);
        iv_stickerInUse = true;


        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < canvas.getChildCount(); i++) {

                    View child = canvas.getChildAt(i);
                    if (child instanceof StickerImageView) {

                        ((StickerImageView) child).setControlItemsHidden(true);
                    }

                    if (child instanceof StickerTextView) {

                        ((StickerTextView) child).setControlItemsHidden(true);
                    }
                }


            }
        });

    }

    private void showCaptionDialog() {
        final AlertDialog.Builder captionDialog = new AlertDialog.Builder(this);
        captionDialog.setTitle(R.string.caption_size);
        String[] captionDialogItems = {
                getString(R.string.small), "Medium", getString(R.string.large)};
        captionDialog.setItems(captionDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                setCaption(100);

                                captionSizeID = 0;
                                break;
                            case 1:
                                setCaption(150);
                                captionSizeID = 1;
                                break;
                            case 2:
                                setCaption(200);
                                captionSizeID = 2;
                                break;

                        }
                    }
                });
        captionDialog.show();
    }


    private void setCaption(int size) {
        ConstraintLayout.LayoutParams paramsLayout = (ConstraintLayout.LayoutParams) canvas.getLayoutParams();
        realHeight = paramsLayout.height;
        paramsLayout.height = paramsLayout.height + size;
        canvas.setLayoutParams(paramsLayout);
        ViewGroup.LayoutParams params = caption.getLayoutParams();
        params.height = size;
        caption.setLayoutParams(params);
        captionText = new TextView(getApplicationContext());
        canvas.addView(captionText);
        captionText.setText("<CAPTION>");
        captionText.setTextColor(Color.BLACK);
        captionTextDialog();
        captionOffBtn.setVisibility(View.VISIBLE);
        captionBtn.setVisibility(View.INVISIBLE);
        imageView.setRotation(SelectImageActivity.ROTATION);
    }

    private void captionTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_text);


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(false);
        input.setMaxLines(4);
        if(captionSizeID == 0) {
            input.setMaxLines(2);
        } else if(captionSizeID == 1) {
            input.setMaxLines(3);
        } else if(captionSizeID == 2) {
            input.setMaxLines(4);
        }
        builder.setView(input);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                captionText.setText(m_Text);
                captionText.setX(caption.getX());
                captionText.setY(caption.getY());

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void editCaptionTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_text);


        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(false);
        input.setMaxLines(4);
        if(captionSizeID == 0) {
            input.setMaxLines(2);
        } else if(captionSizeID == 1) {
            input.setMaxLines(3);
        } else if(captionSizeID == 2) {
            input.setMaxLines(4);
        }
        builder.setView(input);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                captionText.setText(m_Text);

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void resetCaption() {
        ConstraintLayout.LayoutParams paramsLayout = (ConstraintLayout.LayoutParams) canvas.getLayoutParams();
        paramsLayout.height = realHeight;
        canvas.setLayoutParams(paramsLayout);
        ViewGroup.LayoutParams params = caption.getLayoutParams();
        params.height = 0;
        captionText.setText("");
        caption.setLayoutParams(params);
        captionOffBtn.setVisibility(View.INVISIBLE);
        captionBtn.setVisibility(View.VISIBLE);
    }


    public Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void saveImage() {
        for (int i = 0; i < canvas.getChildCount(); i++) {

            View child = canvas.getChildAt(i);
            if (child instanceof StickerImageView) {

                ((StickerImageView) child).setControlItemsHidden(true);
            }
            if (child instanceof StickerTextView) {

                ((StickerTextView) child).setControlItemsHidden(true);
            }
        }


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/StonkIt/Stonks-" + currentDateandTime + ".png");
            viewToBitmap(canvas).compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
            simpleToast(getString(R.string.image_saved));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            simpleToast(getString(R.string.error_went_wrong));
        } catch (IOException e) {
            e.printStackTrace();
            simpleToast(getString(R.string.error_should_not_happen));
        }
    }

    private void shareImage() {
        for (int i = 0; i < canvas.getChildCount(); i++) {

            View child = canvas.getChildAt(i);
            if (child instanceof StickerImageView) {

                ((StickerImageView) child).setControlItemsHidden(true);
            }
            if (child instanceof StickerTextView) {

                ((StickerTextView) child).setControlItemsHidden(true);
            }
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        saveImage();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        final File photoFile = new File(Environment.getExternalStorageDirectory() + "/StonkIt/Stonks-" + currentDateandTime + ".png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
    }


    private void simpleToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }





}
