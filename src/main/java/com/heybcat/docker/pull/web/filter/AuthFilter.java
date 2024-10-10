package com.heybcat.docker.pull.web.filter;

import com.heybcat.docker.pull.web.annoation.WhiteApi;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import com.heybcat.tightlyweb.http.chain.RequestFilterChain.FilterContext;
import com.heybcat.tightlyweb.http.common.Response;
import com.heybcat.tightlyweb.http.entity.WebContext;
import com.heybcat.tightlyweb.http.filter.RequestFilter;
import java.lang.reflect.Method;
import xyz.ldqc.tightcall.protocol.http.HttpNioRequest;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
public class AuthFilter implements RequestFilter {

    @Override
    public void doFilter(WebContext context, FilterContext filterContext) {
        if (StringUtil.isNotBlank(GlobalConfig.getAuthKey()) && !passFilter(context)){
            String auth = context.getRequest().getParam().get("auth");
            if (StringUtil.isBlank(auth) || !auth.equals(GlobalConfig.getAuthKey())){
                filterContext.doResponse(context, Response.unauthorized("Not auth"));
                return;
            }
        }
        filterContext.next(context);
    }

    private boolean passFilter(WebContext context){
        HttpNioRequest request = context.getRequest();
        String path = request.getUri().getPath();
        if (path.startsWith("/web")){
            return true;
        }
        return isWhite(context);
    }

    public boolean isWhite(WebContext context) {
        Method targetMethod = context.getTargetMethod();
        WhiteApi whiteApi = targetMethod.getAnnotation(WhiteApi.class);
        return whiteApi != null;
    }
}
