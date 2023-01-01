##########################################################################
# Copyright 2023 Thoughtworks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################################################################

# Some monkey patching for fixing the url prefix
module TsRoutesExt
  def build_route_function(*args)
    super.gsub(/ return /, ' return "/go" + ')
  end
end

TsRoutes::Generator.send(:prepend, TsRoutesExt)

task generated_js: :environment do
  raise 'OUTPUT_DIR not set' if ENV['OUTPUT_DIR'].blank?

  rm_rf ENV['OUTPUT_DIR']
  mkdir_p ENV['OUTPUT_DIR']

  open(Rails.root.join("#{ENV['OUTPUT_DIR']}/js-routes.js"), 'w') do |f|
    f.puts("// This file is automatically generated by `generated_js` task. Any changes will be lost")
    f.puts("/* eslint-disable */")
    f.puts JsRoutes.generate
  end

  open(Rails.root.join("#{ENV['OUTPUT_DIR']}/ts-routes.ts"), 'w') do |f|
    f.puts("// This file is automatically generated by `generated_js` task. Any changes will be lost")
    f.puts("/* tslint:disable */")
    f.puts TsRoutes.generate(include: JsRoutes.configuration.include)
  end


  open(Rails.root.join("#{ENV['OUTPUT_DIR']}/gocd_version.ts"), 'w') do |f|
    f.puts("// This file is automatically generated by `generated_js` task. Any changes will be lost")
    f.puts("/* tslint:disable */")

    version_map = {
      "version" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().goVersion(),
      "buildNumber" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().distVersion(),
      "gitSha" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().gitRevision(),
      "fullVersion" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().fullVersion(),
      "formattedVersion" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().formatted(),
      "copyrightYear" => com.thoughtworks.go.CurrentGoCDVersion.getInstance().copyrightYear(),
    }
    f.puts "export const GoCDVersion = " + JSON.pretty_generate(version_map)

    f.puts "

      function stripLeadingPrefix(suffix: string, prefix: string) {
        if (suffix.startsWith(prefix)) {
            suffix = suffix.substring(1);
        }
        return suffix;
      }

      export function docsUrl(suffix: string = '') {
        return `#{com.thoughtworks.go.CurrentGoCDVersion.getInstance().baseDocsUrl()}/${stripLeadingPrefix(suffix, '/')}`
      }

      export function apiDocsUrl(suffix: string = '') {
        return `#{com.thoughtworks.go.CurrentGoCDVersion.getInstance().baseApiDocsUrl()}/${stripLeadingPrefix(suffix, '#')}`
      }
    ".strip_heredoc
  end
end
