/*
 * Copyright 2023 Thoughtworks, Inc.
 *
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
package com.thoughtworks.go.util;

import com.thoughtworks.go.domain.materials.Modification;
import com.thoughtworks.go.domain.materials.ModifiedAction;
import com.thoughtworks.go.domain.materials.ModifiedFile;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.thoughtworks.go.util.SvnLogXmlParser.convertDate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SvnLogXmlParserTest {

    private static final String XML = "<?xml version=\"1.0\"?>\n"
        + "<log>\n"
        + "<logentry\n"
        + "   revision=\"3\">\n"
        + "<author>cceuser</author>\n"
        + "<date>2008-03-11T07:52:41.162075Z</date>\n"
        + "<paths>\n"
        + "<path\n"
        + "   action=\"A\">/trunk/revision3.txt</path>\n"
        + "</paths>\n"
        + "<msg>[Liyanhui &amp; Gabbar] Checked in new file for test</msg>\n"
        + "</logentry>\n"
        + "</log>";

    private static final String MULTIPLE_FILES = "<?xml version=\"1.0\"?>\n"
        + "<log>\n"
        + "<logentry\n"
        + "   revision=\"3\">\n"
        + "<author>cceuser</author>\n"
        + "<date>2008-03-11T07:52:41.162075Z</date>\n"
        + "<paths>\n"
        + "<path\n"
        + "   action=\"A\">/trunk/revision3.txt</path>\n"
        + "<path\n"
        + "   action=\"D\">/branch/1.1/readme.txt</path>\n"
        + "</paths>\n"
        + "<msg>[Liyanhui &amp; Gabbar] Checked in new file for test</msg>\n"
        + "</logentry>\n"
        + "</log>";

    @Test
    public void shouldParseSvnLogContainingNullComments() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("jemstep_svn_log.xml"))) {
            String xml = new String(is.readAllBytes(), UTF_8);
            SvnLogXmlParser parser = new SvnLogXmlParser();
            List<Modification> revisions = parser.parse(xml, "", new SAXBuilder());

            assertThat(revisions.size()).isEqualTo(43);
            assertThat(revisions.stream().filter(r -> r.getRevision().equals("7815")).findFirst().orElseThrow().getComment()).isNull();
        }
    }

    @Test
    public void shouldParse() throws ParseException {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> materialRevisions = parser.parse(XML, "", new SAXBuilder());
        assertThat(materialRevisions.size()).isEqualTo(1);
        Modification mod = materialRevisions.get(0);
        assertThat(mod.getRevision()).isEqualTo("3");
        assertThat(mod.getUserName()).isEqualTo("cceuser");
        assertThat(mod.getModifiedTime()).isEqualTo(convertDate("2008-03-11T07:52:41.162075Z"));
        assertThat(mod.getComment()).isEqualTo("[Liyanhui & Gabbar] Checked in new file for test");
        List<ModifiedFile> files = mod.getModifiedFiles();
        assertThat(files.size()).isEqualTo(1);
        ModifiedFile file = files.get(0);
        assertThat(file.getFileName()).isEqualTo("/trunk/revision3.txt");
        assertThat(file.getAction()).isEqualTo(ModifiedAction.added);
    }

    @Test
    public void shouldParseLogEntryWithoutComment() {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> materialRevisions = parser.parse("<?xml version=\"1.0\"?>\n"
            + "<log>\n"
            + "<logentry\n"
            + "   revision=\"3\">\n"
            + "<author>cceuser</author>\n"
            + "<date>2008-03-11T07:52:41.162075Z</date>\n"
            + "<paths>\n"
            + "<path\n"
            + "   action=\"A\">/trunk/revision3.txt</path>\n"
            + "</paths>\n"
            + "</logentry>\n"
            + "</log>", "", new SAXBuilder());
        assertThat(materialRevisions.size()).isEqualTo(1);
        Modification mod = materialRevisions.get(0);
        assertThat(mod.getRevision()).isEqualTo("3");
        assertThat(mod.getComment()).isNull();
    }

    @Test
    public void shouldParseLogWithEmptyRevision() {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> materialRevisions = parser.parse("<?xml version=\"1.0\"?>\n"
            + "<log>\n"
            + "<logentry\n"
            + "   revision=\"2\">\n"
            + "</logentry>\n"
            + "<logentry\n"
            + "   revision=\"3\">\n"
            + "<author>cceuser</author>\n"
            + "<date>2008-03-11T07:52:41.162075Z</date>\n"
            + "<paths>\n"
            + "<path\n"
            + "   action=\"A\">/trunk/revision3.txt</path>\n"
            + "</paths>\n"
            + "</logentry>\n"
            + "</log>", "", new SAXBuilder());
        assertThat(materialRevisions.size()).isEqualTo(1);
        Modification mod = materialRevisions.get(0);
        assertThat(mod.getRevision()).isEqualTo("3");
        assertThat(mod.getComment()).isNull();
    }


    @Test
    public void shouldParseBJCruiseLogCorrectly() {
        String firstChangeLog = "<?xml version=\"1.0\"?>\n"
            + "<log>\n"
            + "<logentry\n"
            + "   revision=\"11238\">\n"
            + "<author>yxchu</author>\n"
            + "<date>2008-10-21T14:00:16.598195Z</date>\n"
            + "<paths>\n"
            + "<path\n"
            + "   action=\"M\">/trunk/test/unit/card_selection_test.rb</path>\n"
            + "<path\n"
            + "   action=\"M\">/trunk/test/functional/cards_controller_quick_add_test.rb</path>\n"
            + "<path\n"
            + "   action=\"M\">/trunk/app/controllers/cards_controller.rb</path>\n"
            + "</paths>\n"
            + "<msg>#2761, fix random test failure and add quick add card type to session</msg>\n"
            + "</logentry>\n"
            + "</log>";

        String secondChangeLog = "<?xml version=\"1.0\"?>\n"
            + "<log>\n"
            + "<logentry\n"
            + "   revision=\"11239\">\n"
            + "<author>yxchu</author>\n"
            + "<date>2008-10-21T14:00:36.209014Z</date>\n"
            + "<paths>\n"
            + "<path\n"
            + "   action=\"M\">/trunk/test/unit/card_selection_test.rb</path>\n"
            + "</paths>\n"
            + "<msg>still fix test</msg>\n"
            + "</logentry>\n"
            + "<logentry\n"
            + "   revision=\"11240\">\n"
            + "<author>yxchu</author>\n"
            + "<date>2008-10-21T14:00:47.614448Z</date>\n"
            + "<paths>\n"
            + "<path\n"
            + "   action=\"M\">/trunk/test/unit/card_selection_test.rb</path>\n"
            + "</paths>\n"
            + "<msg>fix test remove messaging helper</msg>\n"
            + "</logentry>\n"
            + "</log>";

        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> mods = parser.parse(firstChangeLog, ".", new SAXBuilder());
        assertThat(mods.get(0).getUserName()).isEqualTo("yxchu");

        List<Modification> mods2 = parser.parse(secondChangeLog, ".", new SAXBuilder());
        assertThat(mods2.size()).isEqualTo(2);
    }

    @Test
    public void shouldFilterModifiedFilesByPath() {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> materialRevisions = parser.parse(MULTIPLE_FILES, "/branch", new SAXBuilder());

        Modification mod = materialRevisions.get(0);
        List<ModifiedFile> files = mod.getModifiedFiles();
        assertThat(files.size()).isEqualTo(1);
        ModifiedFile file = files.get(0);
        assertThat(file.getFileName()).isEqualTo("/branch/1.1/readme.txt");
        assertThat(file.getAction()).isEqualTo(ModifiedAction.deleted);
    }

    @Test
    public void shouldGetAllModifiedFilesUnderRootPath() {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        List<Modification> materialRevisions = parser.parse(MULTIPLE_FILES, "", new SAXBuilder());

        Modification mod = materialRevisions.get(0);
        List<ModifiedFile> files = mod.getModifiedFiles();
        assertThat(files.size()).isEqualTo(2);

        ModifiedFile file = files.get(0);
        assertThat(file.getFileName()).isEqualTo("/trunk/revision3.txt");
        assertThat(file.getAction()).isEqualTo(ModifiedAction.added);

        file = files.get(1);
        assertThat(file.getFileName()).isEqualTo("/branch/1.1/readme.txt");
        assertThat(file.getAction()).isEqualTo(ModifiedAction.deleted);
    }

    @Test
    public void shouldReportSvnOutputWhenErrorsHappen() {
        SvnLogXmlParser parser = new SvnLogXmlParser();
        assertThatThrownBy(() -> parser.parse("invalid xml", "", new SAXBuilder()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("invalid xml");
    }

    @Test
    public void shouldParseSvnInfoOutputToConstructUrlToRemoteUUIDMapping() {
        final SvnLogXmlParser svnLogXmlParser = new SvnLogXmlParser();
        final String svnInfoOutput = "<?xml version=\"1.0\"?>\n"
            + "<info>\n"
            + "<entry\n"
            + "   kind=\"dir\"\n"
            + "   path=\"trunk\"\n"
            + "   revision=\"3432\">\n"
            + "<url>http://gears.googlecode.com/svn/trunk</url>\n"
            + "<repository>\n"
            + "<root>http://gears.googlecode.com/svn</root>\n"
            + "<uuid>fe895e04-df30-0410-9975-d76d301b4276</uuid>\n"
            + "</repository>\n"
            + "<commit\n"
            + "   revision=\"3430\">\n"
            + "<author>gears.daemon</author>\n"
            + "<date>2010-10-06T02:00:50.517477Z</date>\n"
            + "</commit>\n"
            + "</entry>\n"
            + "</info>";
        final HashMap<String, String> map = svnLogXmlParser.parseInfoToGetUUID(svnInfoOutput, "http://gears.googlecode.com/svn/trunk", new SAXBuilder());
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.get("http://gears.googlecode.com/svn/trunk")).isEqualTo("fe895e04-df30-0410-9975-d76d301b4276");
    }

    @Test
    public void shouldThrowUpWhenSvnInfoOutputIsInvalidToMapUrlToUUID() {
        assertThatThrownBy(() -> new SvnLogXmlParser().parseInfoToGetUUID("Svn threw up and it's drunk", "does not matter", new SAXBuilder()))
            .isInstanceOf(RuntimeException.class);
    }
}
