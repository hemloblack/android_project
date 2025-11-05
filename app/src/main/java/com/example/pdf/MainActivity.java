package com.example.pdf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnAddPdf;
    ListView pdfListView;
    ImageView pdfPreview;
    TextView txtPreviewFileName;
    ArrayList<String> pdfNames = new ArrayList<>();
    ArrayList<String> pdfInternalPaths = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private static final int PICK_PDF_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnAddPdf = findViewById(R.id.btnAddPdf);
        pdfListView = findViewById(R.id.pdfListView);


        try {
            pdfPreview = findViewById(R.id.pdfPreview);
            txtPreviewFileName = findViewById(R.id.txtPreviewFileName);
        } catch (Exception e) {
            pdfPreview = null;
            txtPreviewFileName = null;
        }

        adapter = new ArrayAdapter<String>(this, R.layout.list_item_modern, R.id.txtFileName, pdfNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.txtFileName);

                String fileName = pdfNames.get(position);
                if (fileName.length() > 30) {
                    fileName = fileName.substring(0, 27) + "...";
                }
                textView.setText(fileName);

                return view;
            }
        };

        pdfListView.setAdapter(adapter);

        btnAddPdf.setOnClickListener(v -> openFilePicker());

        pdfListView.setOnItemClickListener((parent, view, position, id) -> {
            String internalPath = pdfInternalPaths.get(position);


            if (isLandscapeMode() && pdfPreview != null && txtPreviewFileName != null) {
                showLandscapePreview(internalPath);
            } else {

                openPdf(internalPath);
            }
        });

        loadExistingFiles();
    }


    private boolean isLandscapeMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    private void showLandscapePreview(String pdfPath) {
        try {
            Bitmap previewBitmap = createSimplePreview(pdfPath);
            if (pdfPreview != null) {
                pdfPreview.setImageBitmap(previewBitmap);
                pdfPreview.setVisibility(View.VISIBLE);
            }


            File file = new File(pdfPath);
            String fileName = file.getName();
            if (txtPreviewFileName != null) {
                txtPreviewFileName.setText(fileName);
            }

            Toast.makeText(this, "پیش‌نمایش نمایش داده شد", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            if (txtPreviewFileName != null) {
                txtPreviewFileName.setText("خطا در نمایش پیش‌نمایش");
            }
            Toast.makeText(this, "خطا در ایجاد پیش‌نمایش", Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap createSimplePreview(String pdfPath) {
        try {
            int width = 400;
            int height = 500;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);


            canvas.drawColor(Color.WHITE);

            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setTextSize(60);
            paint.setTextAlign(Paint.Align.CENTER);


            canvas.drawText("📄", width / 2, height / 2 - 50, paint);


            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            canvas.drawText("PDF File", width / 2, height / 2 + 20, paint);


            paint.setTextSize(20);
            paint.setColor(Color.GRAY);
            File file = new File(pdfPath);
            String fileName = file.getName();
            if (fileName.length() > 20) {
                fileName = fileName.substring(0, 17) + "...";
            }
            canvas.drawText(fileName, width / 2, height / 2 + 70, paint);


            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawRect(5, 5, width - 5, height - 5, paint);

            return bitmap;

        } catch (Exception e) {

            Bitmap bitmap = Bitmap.createBitmap(400, 500, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("خطا در پیش‌نمایش", 200, 250, paint);

            return bitmap;
        }
    }

    private void openPdf(String pdfPath) {
        Intent intent = new Intent(MainActivity.this, PdfViewerActivity.class);
        intent.putExtra("pdfPath", pdfPath);
        startActivity(intent);
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            Intent chooser = Intent.createChooser(intent, "فایل PDF را انتخاب کنید");
            startActivityForResult(chooser, PICK_PDF_REQUEST);

        } catch (Exception e) {
            Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                copyFileToInternalStorage(uri);
                adapter.notifyDataSetChanged();


                if (pdfPreview != null) {
                    pdfPreview.setVisibility(View.GONE);
                }
                if (txtPreviewFileName != null) {
                    txtPreviewFileName.setText("فایلی انتخاب نشده است");
                }
            }
        }
    }

    private void copyFileToInternalStorage(Uri uri) {
        try {
            String fileName = getFileName(uri);
            if (fileName == null) {
                fileName = "document_" + System.currentTimeMillis() + ".pdf";
            }

            if (!fileName.toLowerCase().endsWith(".pdf")) {
                fileName += ".pdf";
            }

            InputStream inputStream = getContentResolver().openInputStream(uri);
            File internalFile = new File(getFilesDir(), fileName);

            FileOutputStream outputStream = new FileOutputStream(internalFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.close();

            if (!pdfNames.contains(fileName)) {
                pdfNames.add(fileName);
                pdfInternalPaths.add(internalFile.getAbsolutePath());
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "فایل اضافه شد: " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "این فایل قبلاً اضافه شده است", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "خطا در کپی فایل: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        try {
            String result = null;
            if (uri.getScheme().equals("content")) {
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex);
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (result == null) {
                result = uri.getLastPathSegment();
            }
            return result;
        } catch (Exception e) {
            return "document_" + System.currentTimeMillis() + ".pdf";
        }
    }

    private void loadExistingFiles() {
        File internalDir = getFilesDir();
        File[] files = internalDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files != null) {
            for (File file : files) {
                pdfNames.add(file.getName());
                pdfInternalPaths.add(file.getAbsolutePath());
            }
            adapter.notifyDataSetChanged();
        }
    }
}