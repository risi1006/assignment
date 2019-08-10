package org.hcilab.projects.nlogx.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.ExportTask;
import org.hcilab.projects.nlogx.service.NotificationHandler;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadLocale();
		setContentView(R.layout.activity_main);

		//change actionbar title,if not it will be system default
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(R.string.app_name));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_delete:
				confirm();
				return true;
			case R.id.menu_export:
				export();
				return true;
			case R.id.menu_changeLang:
				changeLang();

		}
		return super.onOptionsItemSelected(item);
	}


	private void confirm() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
		builder.setTitle(R.string.dialog_delete_header);
		builder.setMessage(R.string.dialog_delete_text);
		builder.setNegativeButton(R.string.dialog_delete_no, (dialogInterface, i) -> {});
		builder.setPositiveButton(R.string.dialog_delete_yes, (dialogInterface, i) -> truncate());
		builder.show();
	}

	private void truncate() {
		try {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_REMOVED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_REMOVED);
			Intent local = new Intent();
			local.setAction(NotificationHandler.BROADCAST);
			LocalBroadcastManager.getInstance(this).sendBroadcast(local);
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

	private void export() {
		if(!ExportTask.exporting) {
			ExportTask exportTask = new ExportTask(this, findViewById(android.R.id.content));
			exportTask.execute();
		}
	}
//	for changing the Language
	private void changeLang() {
		//alert dialog to show the list of language
	 showChangeLanguageDialog();
	}

	private void showChangeLanguageDialog() {
		//array of language to display in alert dialog
		final String[] listItems = {"English","हिंदी","ગુજરાતી","ಕನ್ನಡ"};
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
		mBuilder.setTitle("Choose Language...");
		mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			if(i==0){
				//English
				setLocale("en");
				recreate();
			}
			else if(i==1){
				//Hindi
				setLocale("hi");
				recreate();
			}
			else if(i==2){
				//Gujarati
				setLocale("gu");
				recreate();}
			else if(i==3){
				//Kannada
				setLocale("kn");
				recreate();
				}
			//dismissed alert dialog after selecting the language
			dialogInterface.dismiss();
			}

		});

		AlertDialog mDialog = mBuilder.create();
		//show alert dialog
		mDialog.show();
	}

	private void setLocale(String lang) {
	Locale locale = new Locale(lang);
	Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

		//save data to preferred reference
		SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
		editor.putString("My_Lang",lang);
		editor.apply();

	}

	//Load language saved in shared preference
	public void loadLocale(){
		SharedPreferences pref = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
		String language = pref.getString("My_Lang","");
		setLocale(language);
	}


}