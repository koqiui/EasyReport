package com.easytoolsoft.easyreport.web.config.mvc;

import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.easytoolsoft.easyreport.support.consts.AppEnvConsts;
import com.easytoolsoft.easyreport.support.filter.ContextInitDataFilter;
import com.easytoolsoft.easyreport.web.config.properties.EnvProperties;

/**
 * @author Tom Deng
 **/
@Configuration
@EnableConfigurationProperties(EnvProperties.class)
public class ServletConfig {
	@Autowired
	private EnvProperties envProperties;

	/**
	 * 让static下的静态资源走DefaultServlet, 不走SpringMVC DispatchServlet
	 *
	 * @return ServletRegistrationBean
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		return new ServletRegistrationBean(new DefaultServlet(), "/static/*");
	}

	/**
	 * 在系统启动时加一些初始化数据到request上下文中
	 *
	 * @return FilterRegistrationBean
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public FilterRegistrationBean contextInitDataFilterRegistrationBean() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new ContextInitDataFilter());
		registrationBean.addUrlPatterns("/*");
		registrationBean.addInitParameter(AppEnvConsts.APP_NAME_ITEM, this.envProperties.getAppName());
		registrationBean.addInitParameter(AppEnvConsts.ENV_NAME_ITEM, this.envProperties.getName());
		registrationBean.addInitParameter(AppEnvConsts.VERSION_ITEM, this.envProperties.getVersion());
		System.out.println("当前运行环境：" + this.envProperties.getName());
		registrationBean.setName("contextInitDataFilter");
		return registrationBean;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		final FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
		filterRegistration.addInitParameter("targetFilterLifecycle", "true");
		filterRegistration.setEnabled(true);
		filterRegistration.addUrlPatterns("/*");
		return filterRegistration;
	}

	@Bean
	public ConfigurableServletWebServerFactory containerCustomizer() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

		factory.addErrorPages(new ErrorPage(HttpStatus.UNAUTHORIZED, "/customError/401"));
		factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/customError/403"));
		factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/customError/404"));
		factory.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/customError"));
		return factory;
	}

	@Bean
	public ErrorProperties errorProperties() {
		final ErrorProperties properties = new ErrorProperties();
		properties.setIncludeStacktrace(IncludeStacktrace.ALWAYS);
		return properties;
	}
}
