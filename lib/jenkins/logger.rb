module Jenkins
  class Logger < PageObject

    def self.create(name, loggers)
      visit $jenkins.base_url + '/log/new'
      find(:path, '/name').set name
      click_button 'OK'

      loggers.each do |logger, level|
        click_button 'Add'

        sleep 1

        all(:xpath, '//input[@name="_.name"]').last.set logger
        all(:xpath, '//select[@name="level"]').last.select level
      end

      click_button 'Save'
    end

    def initialize(name)
      super($jenkins.base_url, name)

      begin
        sleep 1
        open
      end until page.has_xpath? "//h1[text()='#{name}']"
    end

    def url
      base_url + "/log/#{name}/"
    end

    def open
      visit url
    end

    def events
      open
      events = []
      all(:css, '#main-panel pre').each do |row|
        matcher = /((?:\/\w+)+.*?) by /.match(row.text)
        next if matcher.nil? # Earlier versions used one element per log entry newer use two
        events << matcher[1]
      end

      return events
    end

    def empty?
      open
      first(:css, '#main-panel pre').nil?
    end
  end
end
