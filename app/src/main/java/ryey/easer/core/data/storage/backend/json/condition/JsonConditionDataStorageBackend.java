/*
 * Copyright (c) 2016 - 2018 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of Easer.
 *
 * Easer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Easer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easer.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.easer.core.data.storage.backend.json.condition;

import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ryey.easer.commons.IllegalStorageDataException;
import ryey.easer.core.data.ConditionStructure;
import ryey.easer.core.data.storage.backend.ConditionDataStorageBackendInterface;
import ryey.easer.core.data.storage.backend.FileDataStorageBackendHelper;
import ryey.easer.core.data.storage.backend.IOUtils;
import ryey.easer.core.data.storage.backend.json.NC;

public class JsonConditionDataStorageBackend implements ConditionDataStorageBackendInterface {

    private static JsonConditionDataStorageBackend instance = null;
    private static Context context = null;
    private static File dir;

    public static JsonConditionDataStorageBackend getInstance(Context context) {
        if (instance == null) {
            if (context != null)
                JsonConditionDataStorageBackend.context = context;
            dir = IOUtils.mustGetSubDir(JsonConditionDataStorageBackend.context.getFilesDir(), "condition");
            instance = new JsonConditionDataStorageBackend();
        }
        return instance;
    }

    private JsonConditionDataStorageBackend() {
    }

    @Override
    public boolean has(String name) {
        return IOUtils.fileExists(dir, name + NC.SUFFIX);
    }

    @Override
    public List<String> list() {
        ArrayList<String> list = new ArrayList<>();
        for (ConditionStructure condition : all()) {
            list.add(condition.getName());
        }
        return list;
    }

    @Override
    public ConditionStructure get(String name) throws FileNotFoundException, IllegalStorageDataException {
        File file = new File(dir, name + NC.SUFFIX);
        return get(file);
    }

    private ConditionStructure get(File file) throws FileNotFoundException, IllegalStorageDataException {
        ConditionParser parser = new ConditionParser();
        return FileDataStorageBackendHelper.get(parser, file);
    }

    @Override
    public void write(ConditionStructure data) throws IOException {
        File file = new File(dir, data.getName() + NC.SUFFIX);
        ConditionSerializer serializer = new ConditionSerializer();
        FileDataStorageBackendHelper.write(serializer, file, data);
    }

    @Override
    public void delete(String name) {
        File file = new File(dir, name + NC.SUFFIX);
        if (!file.delete())
            throw new IllegalStateException("Unable to delete " + file);
    }

    @Override
    public List<ConditionStructure> all() {
        List<ConditionStructure> list = new ArrayList<>();
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    if (pathname.getName().endsWith(NC.SUFFIX)) {
                        return true;
                    }
                }
                return false;
            }
        });
        for (File file : files) {
            try {
                list.add(get(file));
            } catch (IllegalStorageDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e.getCause());
            }
        }
        return list;
    }
}
