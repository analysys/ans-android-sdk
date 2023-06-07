package com.analysys.visual.bind.event;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.ui.WindowUIHelper;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.visual.bind.event.impl.BaseEvent;
import com.analysys.visual.bind.event.impl.EventActionReport;
import com.analysys.visual.bind.event.impl.EventSibAccessibility;
import com.analysys.visual.bind.event.impl.EventSigleAccessibility;
import com.analysys.visual.bind.event.impl.EventWebView;
import com.analysys.visual.bind.property.IProperty;
import com.analysys.visual.bind.property.PropertyFactory;
import com.analysys.visual.bind.property.impl.PropertyText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 将json数据转化为Event对象
 * @Create: 2019-11-28 09:57
 * @author: hcq
 */
public class EventFactory {

    private static final String EVENT_CLICK = "click";
    //    private static final String EVENT_SELECTD = "selected";
    //    private static final String EVENT_GESTURE = "gesture";
    private static final String EVENT_TEXT_CHANGED = "text_changed";
    private static final String EVENT_LONG_CLICK = "long_click";//TODO 待验证
    private static final String EVENT_SIB_CLICK = "sib_click";
    private static final String EVENT_SIB_LONG_CLICK = "sib_long_click";//TODO 待验证

    private static final int EVENT_ACTION_REPORT = 1;
    private static final int EVENT_ACTION_REFLECT = 2;

    public static final String KEY_RECORD_TYPE = "recordtype";
    public static final String KEY_PAYLOAD = "payload";
    public static final String KEY_EVENTS = "events";

    public static final String RECORD_TYPE_ALL = "all";
    public static final String RECORD_TYPE_ADD = "add";
    public static final String RECORD_TYPE_SAVE = "save";
    public static final String RECORD_TYPE_UPDATE = "update";
    public static final String RECORD_TYPE_DELETE = "delete";

    public static BaseEvent createEvent(String recordType, JSONObject joEvent) throws Exception {
        String eventType = joEvent.getString("event_type");
        boolean isHybrid = joEvent.optInt("is_hybrid", 0) == 1;
        // 目标元素
        EventTarget target = new EventTarget(joEvent, isHybrid);

        BaseEvent event = newEvent(eventType, target, isHybrid);
        if (event == null) {
            return null;
        }
        event.target = target;
        event.strEvent = joEvent.toString();

        // 绑定范围
        event.bindingRange = event.new BindingRange();
        if (joEvent.has("binding_range")) {
            JSONObject joRange = joEvent.getJSONObject("binding_range");
            if (!checkVersion(joRange)) {
                return null;
            }
            JSONArray jaPages = joRange.optJSONArray("pages");
            if (jaPages == null || jaPages.length() == 0) {
                event.bindingRange.allActivity = true;
            } else {
                for (int i = 0; i < jaPages.length(); i++) {
                    event.bindingRange.activities.add(jaPages.getString(i));
                }
            }
            JSONArray jaDialogs = joRange.optJSONArray("dialogs");
            if (jaDialogs == null || jaDialogs.length() == 0) {
                event.bindingRange.allDialog = event.bindingRange.allActivity;
                for (String activity : event.bindingRange.activities) {
                    if (activity != null && activity.contains(WindowUIHelper.DIALOG_SUFFIX)) {
                        event.bindingRange.dialogPages.add(activity);
                    } else {
                        event.bindingRange.dialogPages.add(WindowUIHelper.getDialogName(activity));
                    }
                }
            } else {
                for (int i = 0; i < jaDialogs.length(); i++) {
                    event.bindingRange.dialogPages.add(jaDialogs.getString(i));
                }
            }
            JSONArray jaFloatWins = joRange.optJSONArray("floatwins");
            if (jaFloatWins == null || jaFloatWins.length() == 0) {
                event.bindingRange.allFloatWin = event.bindingRange.allActivity;
                for (String activity : event.bindingRange.activities) {
                    if (activity != null && activity.contains(WindowUIHelper.FLOATWIN_SUFFIX)) {
                        event.bindingRange.floatWinPages.add(activity);
                    } else {
                        event.bindingRange.floatWinPages.add(WindowUIHelper.getFloatWindowName(activity));
                    }
                }
            } else {
                for (int i = 0; i < jaFloatWins.length(); i++) {
                    event.bindingRange.floatWinPages.add(jaFloatWins.getString(i));
                }
            }
            JSONArray jaPopWins = joRange.optJSONArray("popwins");
            if (jaPopWins == null || jaPopWins.length() == 0) {
                event.bindingRange.allPopWin = event.bindingRange.allActivity;
                for (String activity : event.bindingRange.activities) {
                    if (activity != null && activity.contains(WindowUIHelper.POPWIN_SUFFIX)) {
                        event.bindingRange.popWinPages.add(activity);
                    } else {
                        event.bindingRange.popWinPages.add(WindowUIHelper.getPopupWindowName(activity));
                    }
                }
            } else {
                for (int i = 0; i < jaPopWins.length(); i++) {
                    event.bindingRange.popWinPages.add(jaPopWins.getString(i));
                }
            }
        } else {
            // 兼容老版本
            String page = joEvent.optString("target_page");
            if (TextUtils.isEmpty(page)) {
                event.bindingRange.allActivity = true;
            } else {
                int idx = page.indexOf(WindowUIHelper.getDialogName(null));
                if (idx == 0) {
                    event.bindingRange.allDialog = true;
                } else if (idx > 0) {
                    event.bindingRange.dialogPages.add(page);
                } else {
                    idx = page.indexOf(WindowUIHelper.getFloatWindowName(null));
                    if (idx == 0) {
                        event.bindingRange.allFloatWin = true;
                    } else if (idx > 0) {
                        event.bindingRange.floatWinPages.add(page);
                    } else {
                        idx = page.indexOf(WindowUIHelper.getPopupWindowName(null));
                        if (idx == 0) {
                            event.bindingRange.allPopWin = true;
                        } else if (idx > 0) {
                            event.bindingRange.popWinPages.add(page);
                        } else {
                            event.bindingRange.activities.add(page);
                        }
                    }
                }
            }
            String appVersion = joEvent.optString("app_version");
            if (!TextUtils.isEmpty(appVersion)) {
                if (!isVersionValid(appVersion)) {
                    return null;
                }
            }
        }
        // 兼容老版本
        if (joEvent.has(RECORD_TYPE_DELETE)) {
            recordType = RECORD_TYPE_DELETE;
        }
        event.recordType = recordType;
        event.id = joEvent.optInt("id", -1);
        event.eventId = joEvent.getString("event_id");

        if (isHybrid) {
            return event;
        }

//        // 同级容器 TODO 第一期无此字段
//        if (joEvent.has("sib")) {
//            event.sibTopTarget = new EventTarget(joEvent.getJSONObject("sib"), false);
//        } else
        if (event.target.listSibTop != null && event.target.listSibTop.size() > 0) {
            EventTarget.SibTop sibTop = event.target.listSibTop.get(0);
            if (sibTop.pathIdx < event.target.path.size() - 1) {
                event.sibTopTarget = new EventTarget(event.target.path, sibTop.pathIdx + 1);
            }
//            if (event.target.sibPosition != -1) {
//                IProperty prop = new PropertySibPosition(PropertyFactory.PROPERTY_SIB_POSITION, null, "number", null, event.target.sibPosition, null);
//                addCondition(event, event.target, prop);
//            }
        }

        // 事件响应
        event.listEventAction = new ArrayList<>();
//        if (joEvent.has("event_action")) { // TODO 第一期无此字段
//            JSONArray jaEventAction = joEvent.getJSONArray("event_action");
//            for (int i = 0; i < jaEventAction.length(); i++) {
//                event.listEventAction.add(createEventAction(jaEventAction.getJSONObject(i)));
//            }
//            if (event.listEventAction.size() == 0) {
//                throw new IllegalArgumentException("event_action error");
//            }
//        } else {
//            // 兼容老版本
        event.listEventAction.add(new EventActionReport());
//        }

        // 属性
        JSONArray jaPro = joEvent.optJSONArray("properties");
        if (jaPro != null && jaPro.length() != 0) {
            event.listProperties = new ArrayList<>();
            for (int i = 0; i < jaPro.length(); i++) {
                IProperty pro = PropertyFactory.createProperty(jaPro.getJSONObject(i));
                if (pro != null) {
                    event.listProperties.add(pro);
                }
            }
        }

        // 关联
        JSONArray jaRelated = joEvent.optJSONArray("related");
        if (jaRelated != null && jaRelated.length() != 0) {
            event.listRelate = new ArrayList<>();
            for (int i = 0; i < jaRelated.length(); i++) {
                JSONObject joRelated = jaRelated.getJSONObject(i);
                BaseEvent.Relate relate = json2Relate(joRelated);
                event.listRelate.add(relate);
            }
        }

        // 条件
//        if (joEvent.has("condition")) { // TODO 第一期无此字段
//            JSONArray jaCondition = joEvent.getJSONArray("condition");
//            for (int i = 0; i < jaCondition.length(); i++) {
//                BaseEvent.EventCondition condition = event.new EventCondition();
//                JSONObject joCondition = jaCondition.getJSONObject(i);
//                JSONObject joTarget = joCondition.optJSONObject("target");
//                if (joTarget == null) {
//                    condition.target = event.target;
//                } else {
//                    condition.target = new EventTarget(joCondition.getJSONObject("target"), false);
//                }
//                JSONArray jaProperty = joCondition.optJSONArray("properties");
//                if (jaProperty != null && jaProperty.length() != 0) {
//                    condition.listProperty = new ArrayList<>();
//                    for (int j = 0; j < jaProperty.length(); j++) {
//                        IProperty pro = PropertyFactory.createProperty(jaProperty.getJSONObject(j));
//                        if (pro != null) {
//                            condition.listProperty.add(pro);
//                        }
//                    }
//                }
////                if (event.sibTopTarget != null) {
////                    condition.sibTopDistance = adjustSibRelatePath(event.sibTopTarget, event.target, condition.target);
////                }
//                if (event.listCondition == null) {
//                    event.listCondition = new ArrayList<>();
//                }
//                event.listCondition.add(condition);
//            }
//        } else {
        // 兼容老版本
        String matchText = joEvent.optString("match_text", null);
        if (!TextUtils.isEmpty(matchText)) {
            IProperty property = new PropertyText(PropertyFactory.PROPERTY_TEXT, null, "string", null, matchText, null);
            addCondition(event, event.target, property);
        }
//        }

//        // 同级事件会绑定所有同级元素，将sib_position属性定位转移到条件里 TODO 待优化
//        if (event.target.propsBindingList != null) {
//            List<IProperty> propsBindingList = event.target.propsBindingList;
//            for (int i = propsBindingList.size() - 1; i >= 0; i--) {
//                IProperty prop = propsBindingList.get(i);
//                if (TextUtils.equals(prop.getName(), PropertyFactory.PROPERTY_SIB_POSITION)) {
//                    addCondition(event, event.target, prop);
//                    propsBindingList.remove(i);
//                    break;
//                }
//            }
//        }
        return event;
    }

    private static void addCondition(BaseEvent event, EventTarget target, IProperty prop) {
        if (event.listCondition == null) {
            event.listCondition = new ArrayList<>();
        }
        BaseEvent.EventCondition condition = event.new EventCondition();
        condition.target = target;
        condition.listProperty = new ArrayList<>();
        condition.listProperty.add(prop);
        event.listCondition.add(condition);
    }

    public static BaseEvent.Relate json2Relate(JSONObject joRelated) throws Exception {
        BaseEvent.Relate relate = new BaseEvent.Relate();
        relate.target = new EventTarget(joRelated.getJSONObject("target"), false);
        // 原生关联h5不需要计算relate细节
        if (relate.target.h5Path == null) {
            JSONArray jaRelatedPro = joRelated.optJSONArray("properties");
            if (jaRelatedPro != null && jaRelatedPro.length() != 0) {
                relate.listProperty = new ArrayList<>();
                for (int j = 0; j < jaRelatedPro.length(); j++) {
                    IProperty pro = PropertyFactory.createProperty(jaRelatedPro.getJSONObject(j));
                    if (pro != null) {
                        relate.listProperty.add(pro);
                    }
                }
            }
        } else {
            relate.strRelate = joRelated.toString();
        }
        return relate;
    }


//    /**
//     * 调整同级事件关联元素的路径，参考{@link com.analysys.visual.bind.event.impl.BaseEvent.Relate#sibTopDistance}
//     */
//    private static int adjustSibRelatePath(EventTarget sibTopTarget, EventTarget eventTarget, EventTarget relateTarget) {
//        if (relateTarget.path == null) {
//            return -1;
//        }
//        // 同级元素路径长度
//        int sibTopPathSize = sibTopTarget.path.size();
//
//        // 事件元素路径长度
//        int eventPathSize = eventTarget.path.size();
//
//        // 关联元素事件长度
//        int relatePathSize = relateTarget.path.size();
//
//        // 异常情况，不应该发生
//        if (eventPathSize <= sibTopPathSize) {
//            return -1;
//        }
//
//        for (int i = 0; i < relatePathSize; i++) {
//            if (i < eventPathSize) {
//                if (!eventTarget.path.get(i).equals(relateTarget.path.get(i))) {
//                    if (i >= sibTopPathSize) {
//                        // 关联元素位于同级容器内
//                        relateTarget.path = relateTarget.path.subList(i - 1, relateTarget.path.size());
//                        if (relateTarget.path.size() > 0) {
//                            // 兼容path匹配算法，设置为-1表示根元素
//                            relateTarget.path.get(0).index = -1;
//                        }
//                        return eventPathSize - i;
//                    } else {
//                        // 关联元素位于同级容器外
//                        return -1;
//                    }
//                }
//            } else {
//                break;
//            }
//        }
//        // 回溯根元素与事件元素相同
//        relateTarget.path = relateTarget.path.subList(eventPathSize, relateTarget.path.size());
//        return 0;
//    }

    private static String sVersionName;

    /**
     * 检查版本是否匹配
     */
    private static boolean checkVersion(JSONObject joRange) throws JSONException {
        JSONArray jaVersions = joRange.optJSONArray("versions");
        if (jaVersions == null || jaVersions.length() == 0) {
            return true;
        }

        for (int i = 0; i < jaVersions.length(); i++) {
            if (isVersionValid(jaVersions.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVersionValid(String version) {
        if (TextUtils.isEmpty(sVersionName)) {
            sVersionName = CommonUtils.getVersionName(AnalysysUtil.getContext());
        }
        return TextUtils.equals(version, sVersionName);
    }

//    /**
//     * 解析事件响应
//     */
//    private static IEventAction createEventAction(JSONObject joEventAction) throws JSONException, ClassNotFoundException {
//        int type = joEventAction.getInt("type");
//        switch (type) {
//            case EVENT_ACTION_REPORT:
//                return new EventActionReport();
//            case EVENT_ACTION_REFLECT:
//                return new EventActionReflect(new ReflectUnit(joEventAction));
//            default:
//                return null;
//        }
//    }

    private static BaseEvent newEvent(String eventType, EventTarget target, boolean isHybrid) {
        if (isHybrid) {
            return new EventWebView(eventType);
        }
        if (target.path != null) {
            if (target.listSibTop != null) {
                return new EventSibAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
        }
        // TODO 暂时只支持 click 事件
        switch (eventType) {
            case EVENT_CLICK:
                return new EventSigleAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_CLICKED);
//            case EVENT_SELECTD:
//                return new EventSigleAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_SELECTED);
//            case EVENT_GESTURE:
//                return new EventSigleAccessibility(eventType, AccessibilityEvent.TYPE_GESTURE_DETECTION_END);
//            case EVENT_TEXT_CHANGED:
//                // 经测试，AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED不会被发送
//                return new EventTextChanged(eventType);
//            case EVENT_LONG_CLICK:
//                return new EventSigleAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
//            case EVENT_SIB_CLICK:
//                return new EventSibAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_CLICKED);
//            case EVENT_SIB_LONG_CLICK:
//                return new EventSibAccessibility(eventType, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
            default:
                return null;
        }
    }
}
