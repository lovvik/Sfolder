package com.example.sfolder;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.documentfile.provider.DocumentFile;


import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;




public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs = null;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private List<String> fileList;
    private ArrayAdapter<String> adapter;
    private DrawerLayout drawerLayout;



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = item.getItemId();



        if (id == R.id.unlock_file){
            ListView v = (ListView) findViewById(R.id.listView);
            long x = adapter.getItemId(info.position);
            String nomdufichier = (String) v.getItemAtPosition((int) x);

            System.out.println("Asked for file to be UNLOKCED " +  nomdufichier);


            // demander le deplacement ici
            try {
                Unlock(nomdufichier);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;

        } else if (id==R.id.delete_file) {

            ListView v = (ListView) findViewById(R.id.listView);
            long x = adapter.getItemId(info.position);
            String nomdufichier = (String) v.getItemAtPosition((int) x);

            System.out.println("Aksed for file to be DELETED " + nomdufichier);

            // demander la suppression ici
            Delete(nomdufichier, x);
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            //System.out.println("Got here for the first run !");
            try {
                GenKey();
            } catch (KeyStoreException  | CertificateException  | IOException  | NoSuchAlgorithmException  | NoSuchProviderException  | InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            }

            File dossier = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"SF_Unlocked");
            if(dossier.exists()) dossier.delete();
            MediaScannerConnection.scanFile(this, new String[]{dossier.getAbsolutePath()}, null, null);
            dossier.mkdir();
            MediaScannerConnection.scanFile(this, new String[]{dossier.getAbsolutePath()}, null, null);



            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("com.example.sfolder", MODE_PRIVATE);
        setContentView(R.layout.activity_main);

        fileList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        // Récupérer la référence de la Toolbar
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // Récupérer la référence du DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout);

        // Récupérer les références des boutons
        ImageButton btnOpenFolder = findViewById(R.id.btnOpenFolder);
        ImageButton btnChangePass = findViewById(R.id.btnResetPassword);


        fileList.clear();
        listFiles();
        adapter.notifyDataSetChanged();

        // Définir les actions onClick pour les boutons
        btnOpenFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOpenFolderClick(v);
            }
        });

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Newpswd.class);
                startActivity(intent);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(">>> clicked on pos : "+position+"id "+ id);
                listView.showContextMenuForChild(view);

                /* récupérer le nom du fichier à traiter*/
                String data = (String) parent.getItemAtPosition(position);
                System.out.println(">>> clicked on : "+data);
            }
        });

    }



    public void onOpenFolderClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        //String[] mimetypes = {"image/*", "video/*", "text/*", "application/*", "" };
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData){

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                try {
                    LockIn(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                //Commenter pour empecher la suppression
                Log.i("message", "Uri file deleted : "+ DocumentFile.fromSingleUri(this, uri).delete());


                fileList.clear();
                listFiles();
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void listFiles() {
        //DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        DocumentFile pickedDir = DocumentFile.fromFile( new File( getExternalFilesDir(null).toString() ));

        if (pickedDir != null && pickedDir.isDirectory()) {
            for (DocumentFile file : pickedDir.listFiles()) {
                fileList.add(file.getName());
            }
        }
    }

    public void onDeleteButtonClick(View view) {
        // Ajoutez votre logique de suppression ici
        // Par exemple, supprimez le fichier sélectionné dans la liste
        if (!fileList.isEmpty()) {
            fileList.remove(0); // Vous pouvez ajuster cette logique selon vos besoins
            adapter.notifyDataSetChanged();
        }
    }

    private void openDrawer() {
        // Ouvrir le tiroir
        drawerLayout.openDrawer(GravityCompat.START);
    }

    // Ajoutez cette méthode pour gérer le clic sur le bouton dans le tiroir
    public void onChangePasswordClick(View view) {
        // Ajoutez ici le code pour gérer le clic sur le bouton "Changer de mot de passe"
    }


    public void LockIn(Uri uri_to_lock) throws IOException{


        /*ContentResolver cr = getContentResolver();
        cr.takePersistableUriPermission(uri_to_lock, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String res = DocumentsContract.findDocumentPath(cr, uri_to_lock).toString();*/
        //System.out.println("read : "+ res);



        //DocumentFile file_to_lock  = DocumentFile.fromSingleUri(this, uri_to_lock);
        DocumentFile srcfile  = DocumentFile.fromSingleUri(this, uri_to_lock);

        InputStream inputStream = null;
        try{
            inputStream = getContentResolver().openInputStream(uri_to_lock);
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        Cipher cipher = null;
        SecretKey secretKey = null;



        try{
            secretKey = GetKey();
        }
        catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e){
            throw new RuntimeException(e);
        }


        try {
            //secretKey = KeyGenerator.getInstance("AES").generateKey();
            //System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey /*, ivSpec*/);
        } catch (java.security.NoSuchAlgorithmException /*| InvalidAlgorithmParameterException */| javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException e){
            e.printStackTrace();
        };

        //System.out.println("got this IV : "+cipher.getIV());


        System.out.println(getExternalFilesDir(null));
        File dstfile = new File(getExternalFilesDir(null), srcfile.getName());
        OutputStream outputStream = new FileOutputStream(dstfile);
        CipherOutputStream out = new CipherOutputStream(outputStream, cipher);

        assert cipher != null;
        outputStream.write(cipher.getIV());
        System.out.println(">> this is the IV used : "+cipher.getIV());

        try{
            //outputStream = new FileOutputStream(dstfile);
            FileUtils.copy(inputStream, out);
            //outputStream = new CipherOutputStream(outputStream, cipher);
            //out.write("Helllo World\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        out.close();
        inputStream.close();
        outputStream.close();


    }

    public void Unlock(String filename) throws IOException {
        File dossier = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"SF_Unlocked");
        if (! dossier.isDirectory()) {
            dossier.mkdir();
            MediaScannerConnection.scanFile(this, new String[]{dossier.getAbsolutePath()}, null, null);

        }
        File srcfile = new File(getExternalFilesDir(null),filename);
        File dstfile  = new File(dossier.getAbsolutePath(),filename);

        dstfile.delete();
        MediaScannerConnection.scanFile(this, new String[]{dstfile.getAbsolutePath()}, null, null);





        InputStream inputStream = new FileInputStream(srcfile);
        OutputStream outputStream = new FileOutputStream(dstfile);

        byte[] iv  = new byte[16];
        inputStream.read(iv);
        System.out.println(">>> IV that has been read : "+iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);



        Cipher cipher = null;
        SecretKey secretKey = null;
        try{
            secretKey = GetKey();
        }
        catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e){
            throw new RuntimeException(e);
        }



        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        } catch (java.security.NoSuchAlgorithmException | InvalidAlgorithmParameterException |javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException e){
            e.printStackTrace();
        };

        CipherOutputStream out = new CipherOutputStream(outputStream, cipher);


        try{
            FileUtils.copy(inputStream, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.close();
        inputStream.close();
        outputStream.close();

        MediaScannerConnection.scanFile(this, new String[]{dstfile.getAbsolutePath()}, null, null);

        Snackbar snackbar = Snackbar.make( findViewById(R.id.mainview), "Fichier disponible dans Download/SF_Unlocked", Snackbar.LENGTH_LONG);
        snackbar.show();


    }


    public void Delete(String name, long id){
        File cible  = new File (getExternalFilesDir(null), name);
        cible.delete();
        fileList.remove(name);
        adapter.notifyDataSetChanged();
        //listFiles();
        //System.out.println(name+" has been deleted");

        Snackbar snackbar = Snackbar.make( findViewById(R.id.mainview), "Fichier supprimé", Snackbar.LENGTH_LONG);
        snackbar.show();
    }


    public void GenKey() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder("SafeFolderSigil", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(256)
                .build();

        keyGenerator.init(keyGenParameterSpec);

        SecretKey secretKey = keyGenerator.generateKey();

        //System.out.println(">>> Key has been generated <<< : "+secretKey);


    }



    public SecretKey GetKey() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey("SafeFolderSigil", null);

        return secretKey;
        //System.out.println(">>>GetKey got<<< : "+secretKey);
    }



}
