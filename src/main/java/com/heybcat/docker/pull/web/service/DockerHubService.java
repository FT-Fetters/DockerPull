package com.heybcat.docker.pull.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.heybcat.docker.pull.web.config.GlobalConfig;
import com.heybcat.docker.pull.web.entity.view.HubSearchView;
import com.heybcat.docker.pull.web.entity.view.HubSearchView.ResultView;
import com.heybcat.tightlyweb.common.ioc.annotation.Cat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ldqc.tightcall.util.StringUtil;

/**
 * @author Fetters
 */
@Cat
public class DockerHubService {

    private static final Logger log = LoggerFactory.getLogger(DockerHubService.class);

    public HubSearchView search(String image, Integer from, Integer size)
        throws IOException, InterruptedException {
        HttpClient httpClient = getHttpClient();
        String targetUri = String.format(
            "https://hub.docker.com/api/search/v3/catalog/search?query=%s&from=%d&size=%d",
            image, from, size);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(targetUri))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            String body = response.body();
            return body2view(body);
        }else {
            return null;
        }
    }

    private HttpClient getHttpClient() {
        Builder clientBuilder = HttpClient.newBuilder();
        if (StringUtil.isNotBlank(GlobalConfig.getProxyHost()) && StringUtil.isNotBlank(
            GlobalConfig.getProxyPort())) {
            clientBuilder.proxy(
                ProxySelector.of(InetSocketAddress.createUnresolved(GlobalConfig.getProxyHost(),
                    Integer.parseInt(GlobalConfig.getProxyPort())))
            );
        }
        return clientBuilder.build();
    }

    private HubSearchView body2view(String body){
        JSONObject bodyObj = JSON.parseObject(body);
        Integer total = bodyObj.getInteger("total");
        JSONArray results = bodyObj.getJSONArray("results");
        int size = results.size();
        List<ResultView> resultViews = new ArrayList<>(size);
        for (Object result : results) {
            JSONObject resultObj = (JSONObject) result;
            ResultView resultView = new ResultView();
            resultView.setId(resultObj.getString("id"));
            resultView.setName(resultObj.getString("name"));
            resultView.setPublisher(resultObj.getJSONObject("publisher").getString("name"));
            JSONObject ratePlan = resultObj.getJSONArray("rate_plans").getJSONObject(0);
            JSONObject repository = ratePlan.getJSONArray("repositories")
                .getJSONObject(0);
            resultView.setNamespace(
               repository.getString("namespace")
            );
            resultView.setDescription(repository.getString("description"));
            resultView.setPullCount(repository.getString("pull_count"));
            resultView.setLogo(resultObj.getJSONObject("logo_url").getString("large"));
            resultViews.add(resultView);
        }
        return new HubSearchView(resultViews, total, size);
    }

    public JSONObject tags(String id, Integer page, Integer size)
        throws IOException, InterruptedException {
        String tagsUri = String.format(
            "https://hub.docker.com/v2/repositories/%s/tags?page_size=%d&page=%d&ordering=last_updated&name="
            ,id, size, page
        );

        HttpClient httpClient = getHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tagsUri))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 200){
            return JSON.parseObject(response.body());
        }else {
            return null;
        }
    }


}
