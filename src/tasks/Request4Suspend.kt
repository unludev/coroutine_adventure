package tasks

import contributors.*

/**
 * GithubService interface imizin icine suspend fonksiyon ekledik geri donus olarakda Responsa sarili User list
 * donduk. Burda o suspend fonksiyonlari calistirmak icin tekrar suspend fonksiyon yazdik
 * Bunun icinde service.getOrgRepos uzerinde istegimizi attik.
 * suspend fonksiyonlar gerekli islemi yapmak icin main threadi askiya alir is bittikten sonra sonucu doner
 * Bir suspend function çağrısı sırasında iş parçacığı diğer görevleri yapmaya devam edebilir, çünkü suspend function
 * çağrısı sırasında iş parçacığı askıya alınmış olur ve diğer görevleri yapmaya devam edebilir.
 *
 * Ayrica suspend fonksiyon yazgidimiz icin execute() fonksiyonu kullanmamiza gerek yoktur.
 * */
suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {

    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}