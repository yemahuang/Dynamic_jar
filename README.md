# Dynamic_jar
# android 插件化动态加载jar
有时候会看到一些应用对应的SDcard里的文件夹里有 ***.jar 等文件，现在明白这些文件大概是用来做应用内自动更新用的。

打比方说，类似eclipse 可以通过预留接口，安装各种插件一样。

　　Android 也可以通过动态加载jar 来实现类似的业务代码更新：（这里所说的jar要通过dx工具来转化成Dalvik byte code，下文会讲到）

注意：首先需要了解一点：在Android中可以动态加载，但无法像Java中那样方便动态加载jar

原因：Dalvik虚拟机如同其他Java虚拟机一样，在运行程序时首先需要将对应的类加载到内存中。而在Java标准的虚拟机中，类加载

可以从class文件中读取，也可以是其他形式的二进制流，因此，我们常常利用这一点，在程序运行时手动加载Class，从而达到

代码动态加载执行的目的。
       然而Dalvik虚拟机毕竟不算是标准的Java虚拟机，因此在类加载机制上，它们有相同的地方，也有不同之处。我们必须区别对待

Android的虚拟机(Dalvik VM)是不认识Java打出jar的byte code，需要通过dx工具来优化转换成Dalvik byte code才行。这一点在

咱们Android项目打包的apk中可以看出：引入其他Jar的内容都被打包进了classes.dex。

 

 

重点：

DexClassLoader类， 这个可以加载jar/apk/dex，也可以从SD卡中加载。

接口类 IDynamic

实现类 Dynamic (打包成jar的类）

Variable类是用来测试这个里面的testValue 变量经过外部定义的变量在jar里能否访问到

别忘记在AndroidManifest.xml 添加权限 

<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

开始上代码：

1.IDynamic

package com.example.test;

import android.content.Context;


public interface IDynamic {
    //预留接口方法
    public void init(Context context);
    
}
 

2.Dynamic 

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
        Toast.makeText(context, Constant.testValue, Toast.LENGTH_LONG).show();
    }

}
3.Variable

package com.example.test;

public class Variable {
    
    
    //用来测试的变量
    public static String testValue = "123456";

}

4.MainActivity

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
        dexPath    需要装载的APK或者Jar文件的路径。包含多个路径用File.pathSeparator间隔开,在Android上默认是 ":" 
        optimizedDirectory    优化后的dex文件存放目录，不能为null
        libraryPath    目标类中使用的C/C++库的列表,每个目录用File.pathSeparator间隔开; 可以为 null
        parent    该类装载器的父装载器，一般用当前执行类的装载器
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


5.打包并转化成dex

选中Dynamic 类，右键 --> Export --> Jar file  



将打包好的jar 拷贝到SDK安装目录android-sdk-windows\platform-tools下，

但是发现现在的SDK这个目录下已经没有dx 文件了，被转移到\build-tools\21.1.1 下

我的目录是：D:\android_sdk\build-tools\21.1.1，这个视个人具体而定，每个人安装的位置不一定一样

接着说，运行Dos cmd命令行进入这个目录

执行命令：

dx --dex --output=dynamic.jar test.jar


完后把test.jar 拷贝到SDcard根目录。

贴MainActivity 加载jar代码

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
        dexPath    需要装载的APK或者Jar文件的路径。包含多个路径用File.pathSeparator间隔开,在Android上默认是 ":" 
        optimizedDirectory    优化后的dex文件存放目录，不能为null
        libraryPath    目标类中使用的C/C++库的列表,每个目录用File.pathSeparator间隔开; 可以为 null
        parent    该类装载器的父装载器，一般用当前执行类的装载器
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
 

贴MainActivity 调用jar里init()方法的代码

        Variable.testValue = "天地不仁，以万物为刍狗";
        
        TextView click = (TextView) findViewById(R.id.click);
        click.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LoadClass().init(MainActivity.this);
            }
        });
6.运行效果图

运行前：把工程里的 Dynamic.java 文件删除掉，这时便知道代码成功调用到jar里的Dynamic.java 了

"天地不仁，以万物为刍狗" 显示，说明 Variable里的 testValue 变量在jar里是可以被调用到的



 

参考文章：

Android动态加载jar/dex
