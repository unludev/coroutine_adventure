package tasks

import contributors.*
import kotlinx.coroutines.*

/**
 * coroutineScope icerisinde yapilacak islemleri asenkron olmasini saglar.
 * async() fonksiyonu geriye Deferred donduru bu ornegimizde bu fonksiyonu deferred bir variable a atiyoruz
 * async(Dispatchers.Default) parametredeki CoroutineDispatcher karsilik gelen coroutinein hangi
 * thread uzerinde calisacagini belirler. Buradaki default thread pooldaki CPU core lari kadar thread saglar.
 */
suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> =
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        val deferred: List<Deferred<List<User>>> = repos.map { repo ->
            async (Dispatchers.Default){
                log("starting loading for ${repo.name}")
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }

        }
        deferred.awaitAll().flatten().aggregate()
    }