package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import kotlin.concurrent.thread

/**
 * bu cagrimda ise background yontemi kullanilmistir. Istek artik UI threadde degilde asagida yazdigimiz
 * BACKGROUND thread icinde atilacaktir. UI threadde asil isi olan UI in calismasini surdurecektir.
 * UIda donma olmayacaktir.
 *
 * UI guncelleyen updateResult fonksiyonu burada higher order olarak kullanilmis butun istek atilip cevap geldikten
 * sonra UI guncelleyecek. SwingUtilities.invokeLater bu fonksiyonun istek cevabi gelince cagrilacagini garanti eder.
 *
 */
fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        updateResults(loadContributorsBlocking(service, req))
    }
}