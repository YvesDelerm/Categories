package fr.ydelerm.bankintest.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.ydelerm.bankintest.BankinApplication
import fr.ydelerm.bankintest.api.BankinApi
import fr.ydelerm.bankintest.model.Category
import fr.ydelerm.bankintest.persistence.ResourceDAO
import fr.ydelerm.bankintest.vo.Status
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class RepositoryImpl(application: Application) : Repository {

    init {
        (application as BankinApplication)
            .appGraph.inject(this)
    }

    companion object {
        private const val LOGTAG = "RepositoryImpl"
    }

    //TODO abstraire en passant par des DataSource
    @Inject
    lateinit var bankinApi: BankinApi
    @Inject
    lateinit var resourceDAO: ResourceDAO

    private val requestStatus = MutableLiveData<Status>()

    override fun refreshData() {
        requestStatus.postValue(Status.LOADING)
        bankinApi.getUsers()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { categoriesResult -> run {
                    resourceDAO.insertResources(categoriesResult.categories)
                    requestStatus.postValue(Status.SUCCESS)
                } },
                { exception ->
                    run {
                        Log.e(LOGTAG, "error while getting  categories", exception)
                        requestStatus.postValue(Status.ERROR)
                    }
                }
            )

    }

    override fun getRequestStatus(): LiveData<Status> {
        return requestStatus
    }

    override fun getCategories(): LiveData<List<Category>> {
        return resourceDAO.getCategories()
    }

    override fun getSubCategories(parentCategoryId: Int): LiveData<List<Category>> {
        return resourceDAO.getSubCategories(parentCategoryId)
    }
}