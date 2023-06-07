package com.analysys.visual.cmd;

import com.analysys.ui.WindowUIHelper;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.VisualManager;
import com.analysys.visual.utils.ViewMethodReflector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SnapshotWrapper {

    private static final String TAG = VisualManager.TAG;
    private static final Class<?>[] NO_PARAMS = new Class[0];

    private PageViewCapture mPageViewCapture;

    public void init(String config) {
        if (mPageViewCapture != null) {
            return;
        }
        try {
            mPageViewCapture = readPageConfig(new JSONObject(config));
        } catch (final Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public byte[] getSnapshotData(boolean forceRefresh) {
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            snapshot2Stream(baos, forceRefresh);
            data = baos.toByteArray();
        } catch (final Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Throwable ignore) {
                }
            }
        }
        return data;
    }

    private void snapshot2Stream(OutputStream out, boolean forceRefresh) {
        final long startSnapshot = System.currentTimeMillis();
        if (mPageViewCapture == null) {
            ANSLog.e(TAG, "error: snapshot is null");
            return;
        }
        ANSLog.i(TAG, "Send ws command: snapshot_response");

        List<WindowUIHelper.PageRootInfo> rootInfo = mPageViewCapture.getListRootViewInfo();

        ANSLog.i(TAG, "snapshot root view " + (rootInfo == null ? "is null" : "size: " + rootInfo.size()));
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(out);
            // 如果页面没有变化
            if (!forceRefresh && (rootInfo == null || rootInfo.isEmpty() || !mPageViewCapture.checkAndUpdateChange(rootInfo))) {
                writer.write("egMsgCode");
                ANSLog.i(TAG, "snapshot page no change");
            } else {
                writer.write("{");
                writer.write("\"type\": \"snapshot_response\",");
                writer.write("\"payload\": {\"new_feature\":1,");
                {
                    writer.write("\"activities\":");
                    writer.flush();
                    mPageViewCapture.capture(out, rootInfo);
                }

                final long snapshotTime = System.currentTimeMillis() - startSnapshot;
                writer.write(",\"snapshot_time_millis\": ");
                writer.write(Long.toString(snapshotTime));

                writer.write("}"); // } payload
                writer.write("}"); // } whole message
                ANSLog.i(TAG, "snapshot send new page");
            }
            writer.flush();
        } catch (final Exception e) {
            ANSLog.e(TAG, "send snapshot server fail", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final Exception e) {
                    ANSLog.e(TAG, "close writer fail", e);
                }
            }
        }
    }

    public PageViewCapture readPageConfig(JSONObject source) throws Exception {
        final List<PageViewInfo> properties = new ArrayList<>();

        try {
            final JSONObject config = source.getJSONObject("config");
            final JSONArray classes = config.getJSONArray("classes");
            for (int classIx = 0; classIx < classes.length(); classIx++) {
                final JSONObject classDesc = classes.getJSONObject(classIx);
                final String targetClassName = classDesc.getString("name");
                final Class<?> targetClass = Class.forName(targetClassName);

                final JSONArray propertyDescs = classDesc.getJSONArray("properties");
                for (int i = 0; i < propertyDescs.length(); i++) {
                    final JSONObject propertyDesc = propertyDescs.getJSONObject(i);
                    final PageViewInfo info = getPageViewInfo(targetClass, propertyDesc);
                    addPageViewInfo(properties, info);
                }
            }

            return new PageViewCapture(properties);
        } catch (JSONException e) {
            throw new Exception("Can't read snapshot configuration", e);
        } catch (final ClassNotFoundException e) {
            throw new Exception("Can't resolve types for snapshot configuration", e);
        }
    }

    private void addPageViewInfo(List<PageViewInfo> listPageViewInfo, PageViewInfo desc) {
        int findIdx = -1;
        for (int i = 0; i < listPageViewInfo.size(); i++) {
            PageViewInfo pageViewInfo = listPageViewInfo.get(i);
            if (pageViewInfo.equals(desc)) {
                findIdx = i;
                if (!(desc.targetClass.isAssignableFrom(pageViewInfo.targetClass))) {
                    return;
                }
                break;
            }
        }
        if (findIdx == -1) {
            listPageViewInfo.add(desc);
        } else {
            listPageViewInfo.set(findIdx, desc);
        }
    }

    private PageViewInfo getPageViewInfo(Class<?> targetClass, JSONObject propertyDesc) throws Exception {
        final String propName = propertyDesc.getString("name");
        ViewMethodReflector accessor = null;
        if (propertyDesc.has("get")) {
            final JSONObject accessorConfig = propertyDesc.getJSONObject("get");
            final String accessorName = accessorConfig.getString("selector");
            final String accessorResultTypeName = accessorConfig.getJSONObject("result")
                    .getString("type");
            final Class<?> accessorResultType = Class.forName(accessorResultTypeName);
            accessor = new ViewMethodReflector(targetClass, accessorName, NO_PARAMS, accessorResultType);
        }
        return new PageViewInfo(propName, targetClass, accessor);
    }

    public void clear() {
        mPageViewCapture = null;
    }
}
