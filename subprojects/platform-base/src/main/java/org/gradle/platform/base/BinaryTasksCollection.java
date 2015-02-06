/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.platform.base;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;

import java.util.NoSuchElementException;

/**
 * A collection of tasks associated to a binary
 * */
public interface BinaryTasksCollection extends DomainObjectSet<Task> {
    /**
     * The 'lifecycle' task, that can be used to construct this binary.
     */
    Task getBuild();

    <T extends Task> T create(String name, Class<T> type, Action<? super T> config);

    Task get(String name) throws NoSuchElementException;

}
