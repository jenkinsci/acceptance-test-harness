require File.dirname(__FILE__) + "/../pageobject.rb"

module Jenkins
  module Scriptler
    class Page < Jenkins::PageObject

      def initialize(base_url)
        super(base_url, "Scriptler plugin page")
      end

      def url
        "#{base_url}/scriptler"
      end

      def upload_script_from(local_path)
        visit "#{url}/scriptsettings"
        attach_file('file', resource(local_path))
        click_button 'Upload'
        return get_script File.basename(local_path)
      end

      def get_script(id)
        return Script.new(self, id)
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

      def run(opts = {})

        with = opts[:with] || {}

        visit "#{@page.url}/runScript?id=#{@id}"

        if opts.has_key? :on
          find(:xpath, "//option[@value='#{opts[:on]}']").select_option
        end

        if !with.empty?
          find(:path, '/defineParams').click
          index = 0
          with.each_pair { |name, value|
              prefix = '/defineParams/parameters' + if index == 0
                  '' else "[#{index}]"
              end
              find(:path, "#{prefix}/name").set(name)
              find(:path, "#{prefix}/value").set(value)
          }
        end

        click_button 'Run'

        @output = find(:xpath, '//h2[text()="Result"]/../pre').text
        return self
      end
    end
  end
end
