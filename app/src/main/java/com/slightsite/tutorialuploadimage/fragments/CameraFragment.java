package com.slightsite.tutorialuploadimage.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.slightsite.tutorialuploadimage.R;
import com.slightsite.tutorialuploadimage.adapters.ImageListAdapter;
import com.slightsite.tutorialuploadimage.controllers.PictureController;
import com.slightsite.tutorialuploadimage.models.Pictures;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CameraFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;
    private static final String LOG_TAG = "MainActivity";

    private View rootView;

    Button button;
    Button button_done;
    Button button_remove;
    Button btn_minimize;
    ImageView imageView;
    ListView image_list_view;
    LinearLayout list_container;
    LinearLayout btn_container;

    private AlertDialog dialog;

    private ArrayList<HashMap<String, Bitmap>> imageList = new ArrayList<>();
    private ArrayList<HashMap<String,String>> imageListAttributes = new ArrayList<>();
    private int current_preview;
    private int group_id = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_camera,
                container, false);

        verifyStoragePermissions(getActivity());

        button = (Button) rootView.findViewById(R.id.btn_take_picture);
        button_done = (Button) rootView.findViewById(R.id.btn_done);
        button_remove = (Button) rootView.findViewById(R.id.btn_remove);
        btn_minimize = (Button) rootView.findViewById(R.id.btn_minimize);
        imageView = (ImageView) rootView.findViewById(R.id.result_image);
        image_list_view = (ListView) rootView.findViewById(R.id.image_list_view);
        list_container = (LinearLayout) rootView.findViewById(R.id.list_container);
        btn_container = (LinearLayout) rootView.findViewById(R.id.btn_container);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();

            }
        });

        button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setMessage(getResources().getString(R.string.confirm_delete))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                _remove_item();
                                Toast.makeText(
                                        getContext(),
                                        getResources().getString(R.string.delete_success_message),
                                        Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_minimize.setVisibility(View.VISIBLE);
                zoomImageFromThumb(imageView, R.drawable.ic_picture_holder_256);
            }
        });
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        btn_minimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_container.setVisibility(View.VISIBLE);
                btn_minimize.setVisibility(View.GONE);
            }
        });

        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(getResources().getString(R.string.confirm_save))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveImage();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return rootView;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            HashMap<String,Bitmap> map = new HashMap<>();
            map.put("img", imageBitmap);
            imageList.add(map);

            HashMap<String,String> mapAttr = new HashMap<>();
            mapAttr.put("file_name", mCurrentFileName);
            mapAttr.put("file_path", mCurrentPhotoPath);
            mapAttr.put("file_dir", mCurrentFilePath);
            mapAttr.put("file_type", mCurrentFileType);
            mapAttr.put("file_size", ""+ imageBitmap.getByteCount());
            imageListAttributes.add(mapAttr);

            current_preview = imageList.size() - 1;

            button_done.setVisibility(View.VISIBLE);
            button_remove.setVisibility(View.VISIBLE);
            list_container.setVisibility(View.VISIBLE);

            rebuildTheImageList();
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            // Get the dimensions of the View
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            imageView.setImageBitmap(bitmap);

            HashMap<String,Bitmap> map = new HashMap<>();
            map.put("img", bitmap);
            imageList.add(map);

            HashMap<String,String> mapAttr = new HashMap<>();
            mapAttr.put("file_name", mCurrentFileName);
            mapAttr.put("file_path", mCurrentPhotoPath);
            mapAttr.put("file_dir", mCurrentFilePath);
            mapAttr.put("file_type", mCurrentFileType);
            mapAttr.put("file_size", "0");
            imageListAttributes.add(mapAttr);

            current_preview = imageList.size() - 1;

            button_done.setVisibility(View.VISIBLE);
            button_remove.setVisibility(View.VISIBLE);
            list_container.setVisibility(View.VISIBLE);

            rebuildTheImageList();
        }
    }

    private void rebuildTheImageList() {
        ImageListAdapter adapter = new ImageListAdapter(getActivity(), imageList);
        image_list_view.setAdapter(adapter);
        imageListViewListener();
    }

    private void imageListViewListener() {
        image_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageView.setImageBitmap(imageList.get(position).get("img"));
                current_preview = position;
            }
        });
    }

    private void _remove_item() {
        File file = new File(imageListAttributes.get(current_preview).get("file_path"));
        boolean deleted = false;
        if (file.isFile()) {
            deleted = file.delete();
        }

        imageList.remove(current_preview);
        imageListAttributes.remove(current_preview);
        int size = imageList.size();
        if (size == 0) {
            button_done.setVisibility(View.GONE);
            button_remove.setVisibility(View.GONE);
            list_container.setVisibility(View.GONE);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_picture_holder_256));
        } else {
            rebuildTheImageList();
            imageView.setImageBitmap(imageList.get(size-1).get("img"));
        }
    }

    public void saveImage() {
        // save to dbs
        for (int i = 0; i < imageListAttributes.size(); i++) {
            File file = new File(storageDir.getPath() + "/" + imageListAttributes.get(i).get("file_name"));
            int file_size = Integer.parseInt(String.valueOf(file.length()/1024));

            Pictures pictures = new Pictures(
                    imageListAttributes.get(i).get("file_name"),
                    imageListAttributes.get(i).get("file_type"),
                    file_size,
                    imageListAttributes.get(i).get("file_dir"),
                    group_id
            );
            int id = PictureController.getInstance().addPicture(pictures);
        }

        imageList.clear();
        imageListAttributes.clear();

        button_done.setVisibility(View.GONE);
        button_remove.setVisibility(View.GONE);
        list_container.setVisibility(View.GONE);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_picture_holder_256));

        //remove the backup if any
        String[] fileNames = storageDir.list();

        for (String fileName : fileNames) {
            File file = new File(storageDir.getPath() + "/" + fileName);
            int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
            if (file_size == 0)
                file.delete();
        }

        Toast.makeText(
                getContext(),
                getResources().getString(R.string.save_success_message),
                Toast.LENGTH_SHORT).show();
    }

    String mCurrentPhotoPath;
    String mCurrentFileName;
    String mCurrentFilePath;
    String mCurrentFileType;
    private File storageDir;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "IMG" + timeStamp + "_";

        storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + File.separator +
                        getResources().getString(R.string.folder_name) + File.separator);

        if (!storageDir.isDirectory()) {
            if (!storageDir.mkdirs()) {
                Log.e(getActivity().getClass().getSimpleName(), "Directory not created");
            }
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentFileName = image.getName();
        mCurrentFilePath = storageDir.getPath();

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = image.getName().lastIndexOf('.')+1;
        String ext = image.getName().substring(index).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);
        mCurrentFileType = type;

        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    private Intent takePictureIntent;
    private File photoFile = null;

    private void dispatchTakePictureIntent() {
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    public void zoomImageFromThumb(final View thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) rootView.findViewById(R.id.expanded_image);
        if (imageList.size() > 0) {
            expandedImageView.setImageBitmap(imageList.get(current_preview).get("img"));
        } else {
            expandedImageView.setImageResource(imageResId);
        }

        list_container.setVisibility(View.GONE);
        btn_container.setVisibility(View.GONE);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        rootView.findViewById(R.id.fcamera_container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                if (imageList.size() > 0) {
                    list_container.setVisibility(View.VISIBLE);
                }
                btn_container.setVisibility(View.VISIBLE);

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    public void closeDialog(View view) {
        dialog.hide();
    }
}
