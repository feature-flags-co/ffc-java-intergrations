package co.featureflags.spring;

import co.featureflags.commons.model.FFCUser;
import co.featureflags.commons.model.UserTag;
import co.featureflags.server.exterior.FFCClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FFCUserUtils {

    public static FFCUser captureUser(HttpServletRequest request, FFCClient client) {
        Map<UserTag, String> tags = new HashMap<>();
        for (UserTag tag : client.getAllUserTags()) {
            String value = getTagValue(request, tag);
            if (value != null) {
                tags.put(tag, value);
            }
        }
        return FFCUser.of(tags);
    }

    private static String getTagValue(HttpServletRequest request, UserTag tag) {
        switch (tag.getSource()) {
            case UserTag.HEADER:
                return request.getHeader(tag.getRequestProperty());
            case UserTag.COOKIE:
                Cookie[] cookies = request.getCookies();
                cookies = cookies == null ? new Cookie[]{} : cookies;
                Cookie res = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(tag.getRequestProperty())).findFirst().orElse(null);
                return res != null ? res.getValue() : null;
            case UserTag.QUERY_STRING:
                return request.getParameter(tag.getRequestProperty());
            case UserTag.POST_BODY:
                if ("POST".equalsIgnoreCase(request.getMethod())) {
                    try {
                        JsonObject json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
                        return json.get(tag.getRequestProperty()).getAsString();
                    } catch (Exception ignore) {
                    }
                }
                return null;
            default:
                return null;
        }
    }
}
