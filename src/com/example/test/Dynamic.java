package com.example.test;

import android.content.Context;
import android.widget.Toast;

/**
 * 接口实现类，即打包成jar的代码
 * @author yema
 *
 */
public class Dynamic implements IDynamic{

	@Override
	public void init(Context context) {
		// TODO Auto-generated method stub
		Toast.makeText(context, Variable.testValue, Toast.LENGTH_LONG).show();
	}

}
