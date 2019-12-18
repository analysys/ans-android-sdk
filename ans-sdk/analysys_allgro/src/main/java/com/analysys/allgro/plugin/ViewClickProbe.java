package com.analysys.allgro.plugin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TabHost;

import com.analysys.allgro.AllegroUtils;
import com.analysys.process.AgentProcess;
import com.analysys.utils.Constants;

import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: fengzeyuan
 * Date: 2019-11-05 22:20
 * Version: 1.0
 */
class ViewClickProbe extends ASMHookAdapter {

    @Override
    public void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }
            trackMenuItem(null, menuItem, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackMenuItem(Object object, MenuItem menuItem, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = null;
            if (!AllegroUtils.isPage(object)) {
                pageObj = AllegroUtils.getPageObjFromView(null);
            }

            if (!checkTrackClickEnable(pageObj, menuItem, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> elementInfo = new HashMap<>();
            elementInfo.put(Constants.ELEMENT_TYPE, "MenuItem");
            CharSequence title = menuItem.getTitle();
            if (!TextUtils.isEmpty(title)) {
                elementInfo.put(Constants.ELEMENT_CONTENT, title);
            }
            String idName = AllegroUtils.getIdResourceName(menuItem.getItemId());
            if (!TextUtils.isEmpty(idName)) {
                elementInfo.put(Constants.ELEMENT_ID, idName);
            }
            autoTrackClick(pageObj, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackTabLayout(Object object, Object tab, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            boolean isTab = false;
            try {
                Class<?> tabCLass = Class.forName("android.support.design.widget.TabLayout$Tab");
                isTab = tabCLass.isInstance(tab);
            } catch (Exception ignored) {
            }

            try {
                Class<?> tabCLass = Class.forName("com.google.android.material.tabs.TabLayout$Tab");
                isTab = tabCLass.isInstance(tab);
            } catch (Exception ignored) {
            }

            if (!isTab) {
                return;
            }

            Object result = AllegroUtils.getFieldValue(tab, "view");
            if (result == null) {
                result = AllegroUtils.getFieldValue(tab, "mView");
            }

            Map<String, Object> elementInfo = new HashMap<>();
            elementInfo.put(Constants.ELEMENT_TYPE, "TabLayout");
            Object pageObj = null;
            View rootView;
            if (!AllegroUtils.isPage(object)) {
                if (object instanceof View) {
                    rootView = (View) object;
                    pageObj = AllegroUtils.getPageObjFromView(rootView);
                } else if (result instanceof View) {
                    rootView = (View) result;
                    pageObj = AllegroUtils.getPageObjFromView(rootView);
                    String idName = AllegroUtils.getViewIdResourceName(rootView);
                    if (!TextUtils.isEmpty(idName)) {
                        elementInfo.put(Constants.ELEMENT_ID, idName);
                    }
                }
            }

            if (!checkTrackClickEnable(pageObj, tab, hasTrackClickAnn)) {
                return;
            }

            result = AllegroUtils.callMethod(tab, "getText");
            if (result instanceof CharSequence) {
                elementInfo.put(Constants.ELEMENT_CONTENT, result.toString());
            }

            autoTrackClick(pageObj, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackTabHost(String tabName, boolean hasTrackClickAnn) {
        try {

            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = AllegroUtils.getPageObjFromView(null);
            TabHost tabHost = (TabHost) Class.forName(TabHost.class.getName()).newInstance();
            if (!checkTrackClickEnable(pageObj, tabHost, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> elementInfo = new HashMap<>();
            elementInfo.put(Constants.ELEMENT_TYPE, "TabHost");
            elementInfo.put(Constants.ELEMENT_CONTENT, tabName);
            autoTrackClick(pageObj, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackDialog(DialogInterface dialogInterface, int which, boolean hasTrackClickAnn) {

        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }

            if (!checkTrackClickEnable(dialog, dialog, hasTrackClickAnn)) {
                return;
            }


            Map<String, Object> elementInfo = new HashMap<>();
            elementInfo.put(Constants.ELEMENT_TYPE, "Dialog");
            // View
            if (dialog instanceof android.app.AlertDialog) {
                android.app.AlertDialog alertDialog = (android.app.AlertDialog) dialog;
                Button button = alertDialog.getButton(which);
                if (button != null) {
                    String text = button.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        elementInfo.put(Constants.ELEMENT_CONTENT, text);
                    }
                    String IdName = AllegroUtils.getViewIdResourceName(button);
                    if (!TextUtils.isEmpty(IdName)) {
                        elementInfo.put(Constants.ELEMENT_ID, IdName);
                    }
                } else {
                    ListView listView = alertDialog.getListView();
                    if (listView != null) {
                        ListAdapter listAdapter = listView.getAdapter();
                        Object object = listAdapter.getItem(which);
                        if (object instanceof String) {
                            elementInfo.put(Constants.ELEMENT_CONTENT, object);
                        }
                        elementInfo.put(Constants.ELEMENT_POSITION, which);
                    }
                }

            } else {
                Class<?> supportAlertDialogClass = null;
                Class<?> androidXAlertDialogClass = null;
                Class<?> currentAlertDialogClass;
                try {
                    supportAlertDialogClass = Class.forName("android.support.v7.app.AlertDialog");
                } catch (Exception ignored) {
                }

                try {
                    androidXAlertDialogClass = Class.forName("androidx.appcompat.app.AlertDialog");
                } catch (Exception ignored) {
                }

                if (supportAlertDialogClass == null && androidXAlertDialogClass == null) {
                    return;
                }

                if (supportAlertDialogClass != null) {
                    currentAlertDialogClass = supportAlertDialogClass;
                } else {
                    currentAlertDialogClass = androidXAlertDialogClass;
                }

                if (currentAlertDialogClass.isInstance(dialog)) {
                    Button button = null;
                    try {
                        Method getButtonMethod = dialog.getClass().getMethod("getButton", int.class);
                        button = (Button) getButtonMethod.invoke(dialog, which);
                    } catch (Exception ignored) {
                    }

                    if (button != null) {
                        String text1 = button.getText().toString();
                        if (!TextUtils.isEmpty(text1)) {
                            elementInfo.put(Constants.ELEMENT_CONTENT, text1);
                        }
                        String IdName = AllegroUtils.getViewIdResourceName(button);
                        if (!TextUtils.isEmpty(IdName)) {
                            elementInfo.put(Constants.ELEMENT_ID, IdName);
                        }
                    } else {
                        try {
                            Method getListViewMethod = dialog.getClass().getMethod("getListView");
                            ListView listView = (ListView) getListViewMethod.invoke(dialog);
                            if (listView != null) {
                                ListAdapter listAdapter = listView.getAdapter();
                                Object object = listAdapter.getItem(which);
                                if (object instanceof String) {
                                    elementInfo.put(Constants.ELEMENT_CONTENT, object);
                                }
                                elementInfo.put(Constants.ELEMENT_POSITION, which);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            autoTrackClick(dialog, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackDrawerSwitch(View drawerLayout, boolean isOpen, boolean hasTrackClickAnn) {
        try {
            Object pageObj = AllegroUtils.getPageObjFromView(drawerLayout);
            if (!checkTrackClickEnable(pageObj, drawerLayout, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> elementInfo = new HashMap<>();
            elementInfo.put(Constants.ELEMENT_TYPE, "DrawerLayout");
            elementInfo.put(Constants.ELEMENT_CONTENT, isOpen ? "Open" : "Close");
            autoTrackClick(pageObj, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }


    @Override
    public void trackRadioGroup(RadioGroup parent, int checkedId, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }


            Object pageObj = AllegroUtils.getPageObjFromView(parent);
            View childView = parent.findViewById(checkedId);
            if (!checkTrackClickEnable(pageObj, childView, hasTrackClickAnn) || !checkTrackClickEnable(pageObj, parent, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> elementInfo = new HashMap<>();
            String[] viewTypeAndText = AllegroUtils.getViewTypeAndText(childView);
            elementInfo.put(Constants.ELEMENT_TYPE, viewTypeAndText[0]);
            elementInfo.put(Constants.ELEMENT_CONTENT, viewTypeAndText[1]);
            elementInfo.put(Constants.ELEMENT_POSITION, parent.indexOfChild(childView) + "");
            autoTrackClick(pageObj, elementInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackListView(AdapterView<?> parent, View v, int position, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = AllegroUtils.getPageObjFromView(parent);
            if (!checkTrackClickEnable(pageObj, v, hasTrackClickAnn) || !checkTrackClickEnable(pageObj, parent, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> viewInfo = new HashMap<>();
            String[] viewTypeAndText = AllegroUtils.getViewTypeAndText(v);
            String viewType = "ListViewItem:" + viewTypeAndText[0];
            String viewText = viewTypeAndText[1];
            viewInfo.put(Constants.ELEMENT_TYPE, viewType);
            viewInfo.put(Constants.ELEMENT_CONTENT, viewText);
            viewInfo.put(Constants.ELEMENT_POSITION, position + "");

            String idName = AllegroUtils.getViewIdResourceName(v);
            if (!TextUtils.isEmpty(idName)) {
                viewInfo.put(Constants.ELEMENT_ID, idName);
            }

            autoTrackClick(pageObj, viewInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackExpListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, boolean hasTrackClickAnn) {
        try {

            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = AllegroUtils.getPageObjFromView(parent);
            if (!checkTrackClickEnable(pageObj, v, hasTrackClickAnn) || !checkTrackClickEnable(pageObj, parent, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> viewInfo = new HashMap<>();
            String[] viewTypeAndText = AllegroUtils.getViewTypeAndText(v);
            String viewType = "ExpandableListViewChildItem:" + viewTypeAndText[0];
            viewInfo.put(Constants.ELEMENT_TYPE, viewType);
            viewInfo.put(Constants.ELEMENT_CONTENT, viewTypeAndText[1]);
            viewInfo.put(Constants.ELEMENT_POSITION, groupPosition + ":" + childPosition);

            String idName = AllegroUtils.getViewIdResourceName(v);
            if (!TextUtils.isEmpty(idName)) {
                viewInfo.put(Constants.ELEMENT_ID, idName);
            }

            autoTrackClick(pageObj, viewInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    @Override
    public void trackExpListViewGroupClick(ExpandableListView parent, View v, int groupPosition, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = AllegroUtils.getPageObjFromView(parent);
            if (!checkTrackClickEnable(pageObj, v, hasTrackClickAnn) || !checkTrackClickEnable(pageObj, parent, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> viewInfo = new HashMap<>();
            String[] viewTypeAndText = AllegroUtils.getViewTypeAndText(v);
            String viewType = "ExpandableListViewGroupItem:" + viewTypeAndText[0];
            viewInfo.put(Constants.ELEMENT_TYPE, viewType);
            viewInfo.put(Constants.ELEMENT_CONTENT, viewTypeAndText[1]);
            viewInfo.put(Constants.ELEMENT_POSITION, groupPosition);

            String idName = AllegroUtils.getViewIdResourceName(v);
            if (!TextUtils.isEmpty(idName)) {
                viewInfo.put(Constants.ELEMENT_ID, idName);
            }

            trackListView(parent, v, groupPosition, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }


    @Override
    public void trackViewOnClick(View v, boolean hasTrackClickAnn) {
        try {
            if (isTrackClickSwitchClose()) {
                return;
            }

            Object pageObj = AllegroUtils.getPageObjFromView(v);
            if (!checkTrackClickEnable(pageObj, v, hasTrackClickAnn)) {
                return;
            }

            Map<String, Object> viewInfo = new HashMap<>();
            String[] viewTypeAndText = AllegroUtils.getViewTypeAndText(v);
            viewInfo.put(Constants.ELEMENT_TYPE, viewTypeAndText[0]);
            viewInfo.put(Constants.ELEMENT_CONTENT, viewTypeAndText[1]);

            String idName = AllegroUtils.getViewIdResourceName(v);
            if (!TextUtils.isEmpty(idName)) {
                viewInfo.put(Constants.ELEMENT_ID, idName);
            }

            autoTrackClick(pageObj, viewInfo, hasTrackClickAnn);
        } catch (Exception e) {
            AllegroUtils.reportCatchException(e);
        }
    }

    private void autoTrackClick(Object pageObj, Map<String, Object> elementInfo, boolean hasTrackClickAnn) throws JSONException {
        if (pageObj != null) {
            // 获取页面相关信息
            elementInfo.putAll(AllegroUtils.getPageInfo(pageObj));
            // 去除此字段
            elementInfo.remove(Constants.PARENT_URL);
        }
        AgentProcess.getInstance().autoTrackViewClick(elementInfo);
    }


    // -------------------------- 黑白名单 ----------------------------------------


    /**
     * 判断是否触发Click上报
     *
     * @param element          被点击控件
     * @param hasTrackClickAnn 是否有附属注解
     */
    private boolean checkTrackClickEnable(Object pageObj, Object element, boolean hasTrackClickAnn) {
        AgentProcess instance = AgentProcess.getInstance();
        if (element instanceof View) {
            View v = (View) element;
            boolean isByUser = v.isPressed();
            if (v instanceof RatingBar || v instanceof CompoundButton) {
                if (!isByUser) {
                    return false;
                }
            }
        }

        boolean isInBlack = instance.isThisViewInAutoClickBlackList(element)
                || (element != null && instance.isThisViewTypeInAutoClickBlackList(element.getClass()))
                || (pageObj != null && instance.isThisPageInAutoClickBlackList(pageObj.getClass().getName()));
        if (isInBlack) {
            return false;
        } else if (AgentProcess.getInstance().hasAutoClickWhiteList()) {
            return instance.isThisViewInAutoClickWhiteList(element)
                    || (element != null && instance.isThisViewTypeInAutoClickWhiteList(element.getClass()))
                    || (pageObj != null && instance.isThisPageInAutoClickWhiteList(pageObj.getClass().getName()));
        }
        return true;
    }

    private static boolean isTrackClickSwitchClose() {
        return !AgentProcess.getInstance().getConfig().isAutoTrackClick();
    }
    
}
