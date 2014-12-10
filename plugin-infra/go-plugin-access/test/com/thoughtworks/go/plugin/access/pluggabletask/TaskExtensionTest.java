/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.plugin.access.pluggabletask;

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.infra.Action;
import com.thoughtworks.go.plugin.infra.ActionWithReturn;
import com.thoughtworks.go.plugin.infra.PluginManager;
import com.thoughtworks.go.plugin.infra.plugininfo.GoPluginDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junitx.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TaskExtensionTest {


    private PluginManager pluginManager;
    private TaskExtension taskExtension;

    @Before
    public void setUp() throws Exception {
        pluginManager = mock(PluginManager.class);
        taskExtension = new TaskExtension(pluginManager);
        PluggableTaskConfigStore.store().setPreferenceFor("APi-task", mock(TaskPreference.class));
        PluggableTaskConfigStore.store().setPreferenceFor("messageBased-task", mock(TaskPreference.class));
    }

    @After
    public void teardown() {
        for (String pluginId : PluggableTaskConfigStore.store().pluginsWithPreference()) {
            PluggableTaskConfigStore.store().removePreferenceFor(pluginId);
        }
    }

    @Test
    public void shouldReportIfThePluginIsMissing() {
        try {
            taskExtension.getExtension("junk");
            fail("expected exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Associated plugin 'junk' not found. Please contact the Go admin to install the plugin."));
        }
    }

    @Test
    public void shouldReturnCorrectTaskExtensionImplForAPIBasedTaskPlugin() {
        String apiBasedPluginId = "APi-task";

        when(pluginManager.hasReferenceFor(Task.class, apiBasedPluginId)).thenReturn(true);

        assertTrue(taskExtension.getExtension(apiBasedPluginId) instanceof ApiBasedTaskExtension);
    }

    @Test
    public void shouldReturnMessageBasedTaskExtensionForMessageBasedTaskPlugin() {
        String messageBasedPluginId = "messageBased-task";

        when(pluginManager.hasReferenceFor(Task.class, messageBasedPluginId)).thenReturn(false);
        when(pluginManager.isPluginOfType(JsonBasedTaskExtension.TASK_EXTENSION, messageBasedPluginId)).thenReturn(true);

        Assert.assertTrue(taskExtension.getExtension(messageBasedPluginId) instanceof JsonBasedTaskExtension);
    }

    @Test
    public void shouldThrowExceptionIfPluginDoesNotImplementEitherMessageOrApiBasedExtension() {
        String pluginId = "messageBased-task";
        when(pluginManager.hasReferenceFor(Task.class, pluginId)).thenReturn(false);
        when(pluginManager.isPluginOfType(JsonBasedTaskExtension.TASK_EXTENSION, pluginId)).thenReturn(false);

        try {
            taskExtension.getExtension(pluginId);
            fail("Should throw exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().equals("Plugin should use either message-based or api-based extension. Plugin-id: " + pluginId));
        }
    }

    @Test
    public void shouldExecuteTheGivenMessageBasedTaskPlugin() {
        final Boolean[] executed = {false};

        String messageBasedPluginId = "messageBased-task";
        when(pluginManager.hasReferenceFor(Task.class, messageBasedPluginId)).thenReturn(false);
        when(pluginManager.isPluginOfType(JsonBasedTaskExtension.TASK_EXTENSION, messageBasedPluginId)).thenReturn(true);

        taskExtension.execute(messageBasedPluginId, new ActionWithReturn<Task, ExecutionResult>() {
            @Override
            public ExecutionResult execute(Task task, GoPluginDescriptor pluginDescriptor) {
                executed[0] = true;
                return null;
            }
        });

        assertTrue(executed[0]);
    }

    @Test
    public void shouldExecuteTheGivenAPIBasedTaskPlugin() {
        String pluginId = "APi-task";
        when(pluginManager.hasReferenceFor(Task.class, pluginId)).thenReturn(true);
        when(pluginManager.isPluginOfType(JsonBasedTaskExtension.TASK_EXTENSION, pluginId)).thenReturn(false);
        ActionWithReturn actionWithReturn = mock(ActionWithReturn.class);
        when(pluginManager.doOn(Task.class, pluginId, actionWithReturn)).thenReturn(ExecutionResult.success("success"));

        ExecutionResult result = taskExtension.execute(pluginId, actionWithReturn);

        verify(pluginManager).doOn(Task.class, pluginId, actionWithReturn);
        assertThat(result.getMessagesForDisplay(), is("success"));
    }

    @Test
    public void shouldDoOnTask() {
        TaskExtension taskExtension = spy(this.taskExtension);
        TaskExtensionContract actualImpl = mock(TaskExtensionContract.class);

        String pluginId = "pluginId";
        doReturn(actualImpl).when(taskExtension).getExtension(pluginId);

        Action action = mock(Action.class);
        taskExtension.doOnTask(pluginId, action);

        verify(actualImpl).doOnTask(pluginId, action);
    }

    @Test
    public void shouldValidateTask() {
        TaskExtension taskExtension = spy(this.taskExtension);
        TaskExtensionContract actualImpl = mock(TaskExtensionContract.class);
        String pluginId = "pluginId";
        TaskConfig taskConfig = mock(TaskConfig.class);
        doReturn(actualImpl).when(taskExtension).getExtension(pluginId);

        taskExtension.validate(pluginId, taskConfig);

        verify(actualImpl).validate(pluginId, taskConfig);
    }
}