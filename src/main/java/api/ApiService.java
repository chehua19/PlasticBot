package api;

import api.models.Changeset;
import api.models.Repository;
import retrofit.http.*;

import java.util.List;

public interface ApiService {
    @GET("/api/v1/repos")
    List<Repository> getAllRepositories();
    @GET("/api/v1/repos/{repname}/changesets")
    List<Changeset> getAllChangesets(@Path("repname") String repositoryName, @Query("q") String date);
}
