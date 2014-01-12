require 'jenkins/pageobject'
require 'jenkins/util/codemirror'

module Jenkins
  module Scriptler
    class Page < Jenkins::PageObject

      def initialize(base_url)
        super(base_url, "Scriptler plugin page")
      end

      def url
        "#{base_url}/scriptler"
      end

      def create_script(id, script)
        new_script
        find(:path, '/id').set(id)
        find(:path, '/name').set(id)

        textarea = Jenkins::Util::CodeMirror.new(page, '/script')
        textarea.set_content script

        click_button 'Submit'
        return get_script id + '.groovy'
      end

      def upload_script_from(local_path)
        new_script
        attach_file('file', resource(local_path))
        click_button 'Upload'
        return get_script File.basename(local_path)
      end

      def get_script(id)
        return Script.new(self, id)
      end

      private
      def new_script
        # Reload until there is some meaningful content.
        # Occasional 404s observed on first load.
        begin
          visit "#{url}/scriptsettings"
        end while !page.has_xpath?("//h1[contains(text(), 'Groovy script')]")
      end
    end

    class Script < Jenkins::PageObject

      attr_accessor :output

      def initialize(page, id)
        super(page.base_url, "Scriplter script: #{id}")
        @page = page
        @id = id
      end

      def delete
        visit "#{@page.url}/removeScript?id=#{@id}"
      end

      def exists?
        visit @page.url
        return all(:xpath, "//a[@href='removeScript?id=#{@id}']").length == 1
      end

      def set_parameters(params)

        visit "#{@page.url}/editScript?id=#{@id}"

        set_params params

        click_button 'Submit'
      end

      def run(opts = {})

        visit "#{@page.url}/runScript?id=#{@id}"

        if opts.has_key? :on
          find(:xpath, "//option[@value='#{opts[:on]}']").select_option
        end

        fill_params(opts[:with] || {})

        click_button 'Run'

        @output = find(:xpath, '//h2[text()="Result"]/../pre').plain
        return self
      end

      private
      def fill_params(params)

        if !params.empty?
          find(:path, '/defineParams').check

          params.each_pair { |name, value|

            value_element = find(:xpath,
                "//input[@name='name' and @value='#{name}']/../input[@name='value']"
            )

            value_element.set value
          }
        end
      end

      def set_params(params)

        if !params.empty?
          find(:path, '/defineParams').check
          index = 0
          params.each_pair { |name, value|

            # click Add Parameter if present
            if index != 0
              add_parameter = '/defineParams/parameters/add_button/repeatable-add'
              button = all(:path, add_parameter).first
              button.click if !button.nil?
            end

            prefix = '/defineParams/parameters' + if index == 0
                '' else "[#{index}]"
            end
            find(:path, "#{prefix}/name").set name
            find(:path, "#{prefix}/value").set value

            index += 1
          }
        end
      end
    end
  end
end
