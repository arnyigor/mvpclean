package com.arny.mvpclean.data.repository.main;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.arny.mvpclean.data.models.CleanFolder;

import java.util.List;

import io.reactivex.Flowable;

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
