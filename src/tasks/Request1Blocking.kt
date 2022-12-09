package tasks

import contributors.*
import retrofit2.Response

/**
 * program BLOCKING olarak calistiginda loadContributorsBlocking fonksiyonu calistirilir,
 * bu fonksiyon execute() methoduyla istekleri senkron olarak alir. Main(UI) thread kullanarak
 * istek yapar bu yuzden program BLOCKING durumundayken pencere donar ve herhangi bir
 * islem yaptirmaz veriler cekilene kadar.
 */
fun loadContributorsBlocking(service: GitHubService, req: RequestData) : List<User> {
    val repos = service
        .getOrgReposCall(req.org)
        .execute() // Istegi yollar ve malum calisan threadi bloklayarak yapar bu isi
        .also { logRepos(req, it) } //istekten donen datanin durumunu loglar
        .body() ?: emptyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributorsCall(req.org, repo.name)
            .execute() // Executes request and blocks the current thread
            .also { logUsers(repo, it) }
            .bodyList() //asagida extension fonksiyonda yazilmistir (istek error loglarsa bos liste donecek)
    }.aggregate()
}

fun <T> Response<List<T>>.bodyList(): List<T> {
    return body() ?: emptyList()
}