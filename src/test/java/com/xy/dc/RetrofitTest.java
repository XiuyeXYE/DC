package com.xy.dc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class RetrofitTest {

	@Test
	public void test() throws IOException {
		Retrofit retrofit = new Retrofit.Builder().baseUrl("http://myptcc.com/Api/").build();
		GitHubService service = retrofit.create(GitHubService.class);
		Call<List<Map<String,Object>>> repos = service.listRepos("Upgrade");
		repos.execute();
	}

	public interface GitHubService {
		@GET("users/{user}/repos")
		Call<List<Map<String,Object>>> listRepos(@Path("user") String user);
	}

	class Repo {

	}
}
