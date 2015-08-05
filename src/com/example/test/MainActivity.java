package com.example.test;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Variable.testValue = "天地不仁，以万物为刍狗";
		
		TextView click = (TextView) findViewById(R.id.click);
		click.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LoadClass().init(MainActivity.this);
			}
		});
		
	}

	/**
	 * 加载sdcard 跟目录的test.jar 
	 * @return IDynamic
	 */
	public IDynamic LoadClass(){
		IDynamic lib = null;
		String dexPath = Environment.getExternalStorageDirectory().toString() + File.separator + "test.jar";
		File dexOutputDirs = this.getApplicationContext().getDir("dex", 0); 
		
		/**
		 * 
		dexPath	需要装载的APK或者Jar文件的路径。包含多个路径用File.pathSeparator间隔开,在Android上默认是 ":" 
		optimizedDirectory	优化后的dex文件存放目录，不能为null
		libraryPath	目标类中使用的C/C++库的列表,每个目录用File.pathSeparator间隔开; 可以为 null
		parent	该类装载器的父装载器，一般用当前执行类的装载器
		 */
		DexClassLoader cl = new DexClassLoader(dexPath,dexOutputDirs.getAbsolutePath(),null,this.getClassLoader());
		try {
			Class<?> libProviderClazz = cl.loadClass("com.example.test.Dynamic");
			lib = (IDynamic)libProviderClazz.newInstance();
		
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return lib;
	}
}
