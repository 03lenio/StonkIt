package de.liquid.stonkit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.liquid.stonkit.stickerview.EditActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SelectImageActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;


    public static Uri contentURIOut;
    public static Bitmap publicBitMap;
    private int GALLERY = 1;
    public static int CAMERA = 2;
    public static float ROTATION = 1;

    private ImageView imageView;
    private Button select;
    private Button rotate;
    private Button reselect;
    private Button next;

    private String IMAGE_DIRECTORY = "StonkIt";
    public static int publicRequestCode;
    public static String publicTempFileCreationTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        setUpDirectories();



        select = findViewById(R.id.choosePictureBtn);
        rotate = findViewById(R.id.rotateBtn);
        reselect = findViewById(R.id.reselectBtn);
        next = findViewById(R.id.nextBtn);
        imageView = findViewById(R.id.editPreset);




        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchActivity();
            }
        });


        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();

            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageView.getRotation() == 0.0) {
                    imageView.setRotation((float) 90.0);
                    ROTATION = 90.0F;
                } else if(imageView.getRotation() == 90.0) {
                    imageView.setRotation((float) 180.0);
                    ROTATION = 180.0F;
                }  else if(imageView.getRotation() == 180.0) {
                    imageView.setRotation((float) 270.0);
                    ROTATION = 270.0F;
                } else if(imageView.getRotation() == 270.0) {
                    imageView.setRotation((float) 360.0);
                    ROTATION = 360.0F;
                } else if(imageView.getRotation() == 360.0) {
                    imageView.setRotation((float) 0.0);
                    ROTATION = 0.0F;
                }
            }
        });

        reselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            //    imageView.getLayoutParams().height = finalHeight;
            //    imageView.getLayoutParams().width = finalWidth;
            }
        });





    }

    @AfterPermissionGranted(213)
    private void selectImage() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(SelectImageActivity.this, perms)) {
            showPictureDialog();



        }else {
            EasyPermissions.requestPermissions(SelectImageActivity.this, getString(R.string.permissions),
                    123, perms);
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }







    private void switchActivity() {

        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
        overridePendingTransition( R.anim.slide_right_in, R.anim.slide_right_out);

    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(R.string.select_action);
        String[] pictureDialogItems = {
                getString(R.string.select_gallery),
                getString(R.string.select_camera)};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void choosePhotoFromGallary() {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    select.setVisibility(View.INVISIBLE);
                    rotate.setVisibility(View.VISIBLE);
                    reselect.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    Toast.makeText(SelectImageActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);
                    contentURIOut = contentURI;
                    publicBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SelectImageActivity.this, R.string.image_failed_to_save, Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            publicRequestCode = requestCode;
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            publicBitMap = thumbnail;
            setUpTempFile();
            contentURIOut =  Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/StonkIt/Stonks-" + publicTempFileCreationTime + ".png"));
            select.setVisibility(View.INVISIBLE);
            rotate.setVisibility(View.VISIBLE);
            reselect.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            Toast.makeText(SelectImageActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

        }
    }



    @Override
    public void onBackPressed() {
        System.out.println("na");
    }


    private void setUpDirectories() {
        String folder_main = "StonkIt";

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private void setUpTempFile() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/StonkIt/Stonks-" + currentDateandTime + ".png");
            publicBitMap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
            publicTempFileCreationTime = currentDateandTime;



        } catch (FileNotFoundException e) {
            e.printStackTrace();
            simpleToast(getString(R.string.error_went_wrong));
        } catch (IOException e) {
            e.printStackTrace();
            simpleToast(getString(R.string.error_should_not_happen));
        }
    }

    private void simpleToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }


}
