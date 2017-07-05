package com.github.autoreply;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by leo on 2016/8/4.
 * 自动回复服务
 */

/**
 * Created by 韦国旺 on 2017/7/4.
 * 自动回复服务
 */
public class AutoReplyService extends AccessibilityService {

    private static final String TAG = AutoReplyService.class.getSimpleName();

    private Handler handler = new Handler();
    private boolean hasNotify = false;
    private static boolean sIsBound = false;

    private String replyStr = "谢谢，等下回复你啊！";

    /**
     * 必须重写的方法，响应各种事件。
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (!Config.isOpenAutoReply) {
            Log.i(TAG, "interrupt auto reply service");
            return;
        }
        int eventType = event.getEventType(); // 事件类型
//        Log.v("llbt", "event type is" + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: // 通知栏事件
                if (PhoneController.isLockScreen(this)) { // 锁屏
                    PhoneController.wakeAndUnlockScreen(this);   // 唤醒点亮屏幕
                }
                openAppByNotification(event);
                hasNotify = true;
                //todo 识别信息
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.v("demo", "text:" + content);
                        if (content.contains("你好")) {
                            replyStr = "大家一起好";
                        } else {
                            replyStr = "现在有事，等下回复你";
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  //窗口事件改变
                openDetailByListView();
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED: //当前界面滚动
                replyThisUI();
                break;
            default:
//                Log.i(TAG, "DEFAULT");
                if (hasNotify) {
                    try {
                        Thread.sleep(1000); // 停1秒, 否则在微信主界面没进入聊天界面就执行了fillInputBar
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (fillInputBar(replyStr)) {
                        findAndPerformAction(UI.BUTTON, "发送");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);   // 返回
                                back2Home(); //TODO 回到系统桌面
                            }
                        }, 1500);

                    }
                    hasNotify = false;
                }
                break;
        }
    }


    /**
     * 在当前界面内回复
     * <p>
     * 判断当前界面是否是聊天界面，界面消息滚动的时候，获取最后一条消息，识别里面的text内容，做自动回复匹配
     */
    private void replyThisUI() {
        List<AccessibilityNodeInfo> lvs = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3e"); //聊天详情页
        if (lvs.size() != 0) {  //不等于0就是在详情界面
            AccessibilityNodeInfo nodeInfo = lvs.get(0);
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(nodeInfo.getChildCount() - 1);
            List<AccessibilityNodeInfo> bodyNodeInfo = childNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/im");
            if (bodyNodeInfo.size() != 0) {
                String bodyText = bodyNodeInfo.get(0).getText().toString();
                Log.v("demo", "body text is ->" + bodyText);
                if (bodyText.contains("你好")) {
                    replyStr = "大家一起好";
                } else {
                    replyStr = "现在有事，等下回复你";
                }
            }
        }
    }

    /**
     * 当前处在微信界面中出现新的消息的时候的处理方式
     */
    private void openDetailByListView() {
        findListView(getRootInActiveWindow());
    }

    /**
     * 回到系统桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt");
    }


    @Override
    protected void onServiceConnected() {
        // mainfest 配置了这里无需配置
//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        info.packageNames = new String[]{Config.WX_PACKAGE_NAME};
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//        info.notificationTimeout = 100;
//        this.setServiceInfo(info);

        Log.i(TAG, "connect auto reply service");
        sIsBound = true;
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "disconnect auto reply service");
        sIsBound = false;
        return super.onUnbind(intent);
    }

    public static boolean isConnected() {
        return sIsBound;
    }

    /**
     * 查找UI控件并点击
     *
     * @param widget 控件完整名称, 如android.widget.Button, android.widget.TextView
     * @param text   控件文本
     */
    private void findAndPerformAction(String widget, String text) {
        // 取得当前激活窗体的根节点
        if (getRootInActiveWindow() == null) {
            return;
        }

        // 通过文本找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        if (nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getClassName().equals(widget) && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK); // 执行点击
                    break;
                }
            }
        }
    }


    /**
     * 打开微信
     *
     * @param event 事件
     */
    private void openAppByNotification(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            try {
                PendingIntent pendingIntent = notification.contentIntent;
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 填充输入框
     */
    private boolean fillInputBar(String reply) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findInputBar(rootNode, reply);
        }
        return false;
    }


    /**
     * 查找EditText控件
     *
     * @param rootNode 根结点
     * @param reply    回复内容
     * @return 找到返回true, 否则返回false
     */
    private boolean findInputBar(AccessibilityNodeInfo rootNode, String reply) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (UI.EDITTEXT.equals(node.getClassName())) {   // 找到输入框并输入文本
                fillText(node, reply);
                node.recycle(); //TODO 尝试回收 不行再注释掉
                return true;
            }

            if (findInputBar(node, reply)) {    // 递归查找
                return true;
            }
        }
        return false;
    }

    /**
     * 查找listView并找到他的子控件的子控件只有一个ImageView的
     *
     * @param rootNode
     * @return
     */
    private boolean findListView(AccessibilityNodeInfo rootNode) {
//        int count = rootNode.getChildCount();
//        for (int i = 0; i < count; i++) {
//            AccessibilityNodeInfo node = rootNode.getChild(i);
//            if (UI.LISTVIEW.equals(node.getClassName())) {   // 找到listview
//                Log.d("llbt", "找到listview ");
//                perFormClickListViewItem(node);
//                return true;
//            }
//
//            if (findListView(node)) {    // 递归查找
//                return true;
//            }
//        }


        List<AccessibilityNodeInfo> lvs = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3e"); //详情页
        List<AccessibilityNodeInfo> lvs2 = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny"); //"微信"页
        if (lvs2.size() != 0 && lvs.size() == 0) {
//            Log.d("llbt", "找到listview");
            perFormClickListViewItem(lvs2.get(0));
        } else {
//            Log.d("llbt", "没有找到listview");
        }

        return false;


    }

    /**
     * 判断点击listView的item
     *
     * @param rootNode
     */
    private void perFormClickListViewItem(AccessibilityNodeInfo rootNode) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            List<AccessibilityNodeInfo> text = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ia");
            if (text.size() != 0) {
                Log.d("llbt", "有未读消息");
                //未读消息匹配回复消息
                List<AccessibilityNodeInfo> body = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agy");
                if (body.size() != 0) {
                    String bodyText = body.get(0).getText().toString();
                    Log.v("demo", "bodyText:" + bodyText);
                    if (bodyText.contains("你好")) {
                        replyStr = "大家一起好";
                    } else {
                        replyStr = "现在有事，等下回复你";
                    }
                }
                //点击事件
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(1000); // 停1秒, 否则在微信主界面没进入聊天界面就执行了fillInputBar
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (fillInputBar(replyStr)) {
                    findAndPerformAction(UI.BUTTON, "发送");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);   // 返回
                        }
                    }, 1500);

                }
            } else {
                Log.d("llbt", "没有未读消息");
            }
        }


//        int count = rootNode.getChildCount();
//        for (int i = 0; i < count; i++) {
//            AccessibilityNodeInfo node = rootNode.getChild(i);
////            Log.d("llbt", "node->" + node.getClassName());
//            if (UI.LL.equals(node.getClassName())) {   // 找到输入框并输入文本
//                if (node.getChildCount() != 0) {
//                    Log.d("llbt", "node（0）->" + node.getChild(0).getClassName());
//                    if (UI.RL.equals(node.getChild(0))) {
//                        if (node.getChild(0).getChildCount() == 2) {
//                            Log.d("llbt", "有未读消息");
//                        } else {
//                            Log.d("llbt", "没有未读消息");
//                        }
//                    }
//                }
//            }


//            if()
//            Log.d("llbt", "rootNode.getChild(i)->" + rootNode.getChild(i).getClassName());
//            int count2 = rootNode.getChild(i).getChildCount();
//            Log.d("llbt", "count2->" + count2);

//            AccessibilityNodeInfo node = rootNode.getChild(i).getc;


//            if (node != null && node.getChildCount() != 0) {
//                if (node.getChild(0).getChildCount() > 1) {
//                    Log.d("llbt", "执行点击");
//                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK); // 执行点击
//                } else {
//                    Log.d("llbt", "无法点击");
//                }
//            }


//        }

    }


    /**
     * 填充文本
     */
    private void fillText(AccessibilityNodeInfo node, String reply) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "set text-->" + reply);
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, reply);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {
            ClipData data = ClipData.newPlainText("reply", reply);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }
}
