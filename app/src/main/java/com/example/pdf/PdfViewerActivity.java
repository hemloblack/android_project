package com.example.pdf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pdfPath = getIntent().getStringExtra("pdfPath");
        if (pdfPath != null && new File(pdfPath).exists()) {
            openPdfWithExternalApp(pdfPath);
        } else {
            Toast.makeText(this, "فایل PDF یافت نشد", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openPdfWithExternalApp(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);

            Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "برنامه‌ای برای باز کردن PDF یافت نشد", Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this, "خطا در باز کردن فایل", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}