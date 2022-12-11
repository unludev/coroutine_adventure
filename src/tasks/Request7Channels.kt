package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        /**
         * Channeldan alınan her yeni liste, tüm kullanıcıların listesine eklenir.
         * Sonucu toplar ve updateResults callbackini kullanarak durumu güncellersiniz:
         */

        val channel = Channel<List<User>>()

        for (repo in repos) {
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }
        var allusers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allusers = (allusers + users).aggregate()
            updateResults(allusers, it == repos.lastIndex)
        }
        /**
         * Farklı repositorylere ait sonuçlar hazır olur olmaz channela eklenir. İlk başta, tüm istekler
         * gönderildiğinde ve hiçbir veri alınmadığında, receove() çağrısı askıya alınır. Bu durumda,
         * tüm "load contributors" coroutine askıya alınır.
         * Ardından, kullanıcı listesi channela gönderildiğinde, "load contributors" coroutine
         * devam eder, receive() çağrısı bu listeyi döndürür ve sonuçlar hemen güncellenir.
         */
    }
 }
