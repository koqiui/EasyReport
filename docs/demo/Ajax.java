package com.swb.common.http;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;

import com.swb.common.exception.ValidationException;
import com.swb.common.model.Result;
import com.swb.common.util.ExceptionUtil;
import com.swb.common.util.JsonUtil;

/**
 * java版ajax
 * 
 * @author koqiui
 * @date 2019年5月29日 下午7:12:51
 *
 */
public class Ajax {
	private final Log logger = LogFactory.getLog(this.getClass());
	//
	private HttpClientX<Object> httpClient;
	private HttpResponseHeaderProvider responseHeaderProvider;

	//
	public Ajax(HttpClientX<Object> httpClient) {
		if (httpClient == null) {
			throw new ValidationException("httpClient不能为null");
		}
		//
		this.httpClient = httpClient;
		this.responseHeaderProvider = this.httpClient.getResponeHeaderProvider();
	}

	public Ajax() {
		this.httpClient = new HttpClientX<>(null, new AjaxResponseHandler(true));
		this.responseHeaderProvider = this.httpClient.getResponeHeaderProvider();
	}

	//
	private static List<HttpNameValuePair> toHttpNameValuePairs(Map<String, Object> params, boolean dupListValueItems) {
		List<HttpNameValuePair> nameValuePairs = new ArrayList<HttpNameValuePair>();
		//
		if (params != null) {
			for (Map.Entry<String, ?> param : params.entrySet()) {
				String name = param.getKey();
				Object value = param.getValue();
				if (value != null) {
					if (dupListValueItems) {
						Class<?> valueClass = value.getClass();
						if (valueClass.isArray()) {
							int length = Array.getLength(value);
							for (int i = 0; i < length; i++) {
								nameValuePairs.add(HttpNameValuePair.newOne(name, Array.get(value, i)));
							}
							continue;
						} else if (List.class.isAssignableFrom(valueClass)) {
							List<?> itemList = (List<?>) value;
							for (int i = 0; i < itemList.size(); i++) {
								nameValuePairs.add(HttpNameValuePair.newOne(name, itemList.get(i)));
							}
							continue;
						}
					}
					nameValuePairs.add(HttpNameValuePair.newOne(name, value));
				}
			}
		}
		//
		return nameValuePairs;
	}

	//
	private String _baseUrl = null;
	private HttpMethod _method = HttpMethod.GET;
	private String _url = null;
	private String _contentType = ContentTypes.APPLICATION_JSON_VALUE;
	private Charset _charset = ContentTypes.CHARSET_UTF8;
	private String _resultType = ContentTypes.APPLICATION_JSON_VALUE;
	private Map<String, String> _headers = new LinkedHashMap<String, String>();
	private Map<String, Object> _params = new LinkedHashMap<String, Object>();
	//
	private Map<String, Object> _dataMap = new LinkedHashMap<String, Object>();
	private String _dataText = null;
	//
	private boolean _debug = false;

	private boolean isFormStyle() {
		return this._contentType.indexOf("-form-") != -1;
	}

	//
	public Ajax baseUrl(String baseUrl) {
		this._baseUrl = baseUrl;
		//
		return this;
	}

	public Ajax get(String url) {
		this._url = url;
		//
		this._method = HttpMethod.GET;
		//
		return this;
	}

	public Ajax post(String url) {
		this._url = url;
		//
		this._method = HttpMethod.POST;
		//
		return this;
	}

	public Ajax put(String url) {
		this._url = url;
		//
		this._method = HttpMethod.PUT;
		//
		return this;
	}

	public Ajax header(String name, String value) {
		if (value == null) {
			_headers.remove(name);
		} else {
			_headers.put(name, value);
		}
		//
		return this;
	}

	public Ajax headers(Map<String, String> headers) {
		if (headers == null || headers.isEmpty()) {
			this._headers.clear();
		} else {
			this._headers.putAll(headers);
		}
		//
		return this;
	}

	public Ajax param(String name, Object value) {
		if (value == null) {
			_params.remove(name);
		} else {
			_params.put(name, value);
		}
		//
		return this;
	}

	public Ajax params(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			this._params.clear();
		} else {
			this._params.putAll(params);
		}
		//
		return this;
	}

	public Ajax data(Object data) {
		if (data == null) {
			this._dataText = null;
		} else {
			this._dataText = JsonUtil.toJson(data);
		}
		//
		return this;
	}

	public Ajax dataMap(Map<String, Object> dataMap) {
		if (dataMap == null || dataMap.isEmpty()) {
			this._dataMap.clear();
		} else {
			this._dataMap.putAll(dataMap);
		}
		//
		return this;
	}

	public Ajax dataText(String dataText) {
		this._dataText = dataText;
		//
		return this;
	}

	//
	public Ajax asForm() {
		this._contentType = ContentTypes.APPLICATION_FORM_URLENCODED_VALUE;
		//
		return this;
	}

	public Ajax asJson() {
		this._contentType = ContentTypes.APPLICATION_JSON_VALUE;
		//
		return this;
	}

	public Ajax asText() {
		this._contentType = ContentTypes.TEXT_PLAIN_VALUE;
		//
		return this;
	}

	public Ajax asXml() {
		this._contentType = ContentTypes.APPLICATION_XML_VALUE;
		//
		return this;
	}

	public Ajax charset(Charset charset) {
		this._charset = charset;
		//
		return this;
	}

	//
	public Ajax forJson() {
		this._resultType = ContentTypes.APPLICATION_JSON_VALUE;
		//
		return this;
	}

	public Ajax forText() {
		this._resultType = ContentTypes.TEXT_PLAIN_VALUE;
		//
		return this;
	}

	public Ajax forXml() {
		this._resultType = ContentTypes.APPLICATION_XML_VALUE;
		//
		return this;
	}

	public Ajax forAll() {
		this._resultType = ContentTypes.ALL_VALUE;
		//
		return this;
	}

	public Ajax debug(boolean debug) {
		this._debug = debug;
		//
		return this;
	}

	// 缓存的最后结果
	private String lastErrorText = null;
	private String lastResultText = null;
	private byte[] lastResultBytes = null;
	private Map<String, String> lastResultHeaders = null;

	//
	public Ajax send() {
		return this.send(false);
	}

	public Ajax send(boolean encodeQryParams) {
		// 清除上次结果数据
		this.lastErrorText = null;
		this.lastResultText = null;
		this.lastResultBytes = null;
		this.lastResultHeaders = null;
		//
		String fullUrl = this._url == null ? "" : this._url;
		if (!HttpUtil.isHttpOrHttpsUrl(fullUrl)) {
			fullUrl = (this._baseUrl == null) ? fullUrl : this._baseUrl + fullUrl;
			if (!HttpUtil.isHttpOrHttpsUrl(fullUrl)) {
				throw new ValidationException("url无效：" + fullUrl);
			}
		}
		//
		String responeType = ContentTypes.extractMimeType(this._resultType);
		boolean isJsonResponse = ContentTypes.isJson(responeType);
		boolean isTextResponse = isJsonResponse;
		if (!isTextResponse) {
			// 只处理常见的的响应类型
			if (ContentTypes.isText(responeType) || ContentTypes.isXml(responeType) || ContentTypes.isHtml(responeType) || ContentTypes.isJavascript(responeType)) {
				isTextResponse = true;
			}
		}
		//
		Object result = null;
		try {
			this.httpClient.setCharset(this._charset);
			this.httpClient.setContentType(ContentType.create(this._contentType));
			this.httpClient.setAccept(ContentTypes.withMimeTypeAll(this._resultType));
			// 加入Ajax请求标记（非常重要！）
			this.header("X-Requested-With", "XMLHttpRequest");
			//
			for (Map.Entry<String, String> headerItem : this._headers.entrySet()) {
				this.httpClient.setRequestHeader(headerItem.getKey(), headerItem.getValue());
			}
			this.httpClient.setEncodeUrlParamValues(encodeQryParams);
			//
			List<HttpNameValuePair> qryParams = toHttpNameValuePairs(this._params, true);
			//
			boolean isForm = this.isFormStyle();
			if (HttpMethod.GET.equals(this._method)) {
				result = this.httpClient.doGetRequestX(fullUrl, qryParams);
			} else if (HttpMethod.POST.equals(this._method)) {
				if (this._dataText != null) {
					result = this.httpClient.doPostRequestX(fullUrl, qryParams, this._dataText);
				} else {
					String urlWithQryParams = HttpUtil.makeUrl(fullUrl, qryParams, null, false);
					List<HttpNameValuePair> dataParams = toHttpNameValuePairs(this._dataMap, isForm);
					result = this.httpClient.doPostRequestX(urlWithQryParams, dataParams);
				}
			} else if (HttpMethod.PUT.equals(this._method)) {
				if (this._dataText != null) {
					result = this.httpClient.doPutRequestX(fullUrl, qryParams, this._dataText);
				} else {
					String urlWithQryParams = HttpUtil.makeUrl(fullUrl, qryParams, null, false);
					List<HttpNameValuePair> dataParams = toHttpNameValuePairs(this._dataMap, isForm);
					result = this.httpClient.doPutRequestX(urlWithQryParams, dataParams);
				}
			} else {
				throw new ValidationException("不支持的http请求方法：" + this._method);
			}
			//
			this.lastResultHeaders = responseHeaderProvider == null ? null : responseHeaderProvider.getResponseHeaders();
		} catch (Exception exp) {
			this.logger.error(exp);
			//
			this.lastErrorText = ExceptionUtil.extractMessage(exp, true);
		}
		// 清除请求数据
		this.headers(null);
		this.params(null);
		this.data(null);
		this.dataMap(null);
		this.dataText(null);
		// 转换缓存结果数据
		if (result != null) {
			if (result instanceof String) {
				this.lastResultText = (String) result;
			} else {
				this.lastResultBytes = (byte[]) result;
			}
		}
		//
		return this;
	}

	//
	public <T> Result<T> resultAsJson(Class<T> TData) {
		return Result.fromJsonToTypedResult(this.lastResultText, TData, true);
	}

	public <T> Result<T> resultAsJson(Class<T> TData, boolean forceEvenOnFail) {
		return Result.fromJsonToTypedResult(this.lastResultText, TData, forceEvenOnFail);
	}

	public String resultAsText() {
		return this.lastResultText;
	}

	public byte[] resultAsBytes() {
		return this.lastResultBytes;
	}

	public Map<String, String> resultHeaders() {
		return this.lastResultHeaders;
	}

	//
	public String getLastErrorText() {
		return this.lastErrorText;
	}

}
