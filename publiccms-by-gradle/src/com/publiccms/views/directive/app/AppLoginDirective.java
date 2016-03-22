package com.publiccms.views.directive.app;

//Generated 2015-5-10 17:54:56 by com.sanluan.common.source.SourceMaker

import static com.sanluan.common.tools.VerificationUtils.encode;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractAppV1Directive;
import com.publiccms.common.base.AbstractController;
import com.publiccms.entities.log.LogLogin;
import com.publiccms.entities.sys.SysApp;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysUserToken;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.sys.SysUserService;
import com.publiccms.logic.service.sys.SysUserTokenService;
import com.sanluan.common.handler.RenderHandler;
import com.sanluan.common.tools.RequestUtils;

@Component
public class AppLoginDirective extends AbstractAppV1Directive {

    public void execute(RenderHandler handler, SysApp app, SysUser user) throws IOException, Exception {
        String username = handler.getString("username");
        String password = handler.getString("password");
        boolean result = false;
        if (notEmpty(username) && notEmpty(password)) {
            SysSite site = getSite(handler);
            if (AbstractController.virifyNotEMail(username)) {
                user = service.findByName(site.getId(), username);
            } else {
                user = service.findByEmail(site.getId(), username);
            }
            String ip = RequestUtils.getIpAddress(handler.getRequest());
            if (user.getPassword().equals(encode(password))) {
                String authToken = UUID.randomUUID().toString();
                sysUserTokenService.save(new SysUserToken(authToken, user.getId(), app.getChannel(), getDate(), ip));
                service.updateLoginStatus(user.getId(), ip);
                logLoginService
                        .save(new LogLogin(site.getId(), username, user.getId(), app.getChannel(), ip, true, getDate(), ip));
                user.setPassword(null);
                result = true;
                handler.put("authToken", authToken).put("user", user);
            } else {
                LogLogin log = new LogLogin();
                log.setName(username);
                log.setErrorPassword(password);
                log.setIp(ip);
                logLoginService.save(log);
            }
        }
        handler.put("result", result).render();
    }

    @Autowired
    private SysUserService service;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Autowired
    private LogLoginService logLoginService;

    @Override
    public boolean needUser() {
        return false;
    }

    @Override
    public boolean needApp() {
        return true;
    }
}