/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.domain.events;

import java.io.File;

import my.home.model.entities.MessageItem;

/**
 * Created by legendmohe on 15/12/5.
 */
public class DRecordingMsgEvent {
    private File resultFile;
    private MessageItem msgItem;

    public DRecordingMsgEvent(File file, MessageItem item) {
        msgItem = item;
        resultFile = file;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }

    public MessageItem getMsgItem() {
        return msgItem;
    }

    public void setMsgItem(MessageItem msgItem) {
        this.msgItem = msgItem;
    }
}
