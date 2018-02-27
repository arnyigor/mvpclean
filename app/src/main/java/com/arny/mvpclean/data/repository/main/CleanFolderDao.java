package com.arny.mvpclean.data.repository.main;

import android.arch.persistence.room.*;
import com.arny.mvpclean.data.models.CleanFolder;
import io.reactivex.Flowable;

import java.util.List;

@Dao
public interface CleanFolderDao {
	@Query("SELECT * FROM folders")
	List<CleanFolder> getCleanFileList();

	@Query("SELECT * FROM folders WHERE _id=:id")
	Flowable<CleanFolder> getCleanFile(long id);

	@Query("DELETE FROM folders WHERE _id=:id")
	int delete(long id);

	@Query("DELETE FROM folders")
	int delete();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	long insert(CleanFolder folder);
}
