package com.example.pdf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    ArrayList<String> pdfNames = new ArrayList<>();
    ArrayList<String> pdfInternalPaths = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private static final int PICK_PDF_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddPdf = findViewById(R.id.btnAddPdf);
        pdfListView = findViewById(R.id.pdfListView);

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
            openPdf(internalPath);
        });

        loadExistingFiles();
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