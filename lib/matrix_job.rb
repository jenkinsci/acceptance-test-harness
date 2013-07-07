require File.dirname(__FILE__) + "/pageobject.rb"
require File.dirname(__FILE__) + "/build.rb"

module Jenkins
  class MatrixJob < Job

    def initialize(*args)
      super(*args)
    end

    def add_user_axis(name,value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       find(:xpath, "//li/a[text()='User-defined Axis']").click
       sleep 0.1 # wait until axis appear
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.TextAxis']//td/input";
       find(:xpath, "(#{input}[@name='_.name'])[last()]").set(name)
       find(:xpath, "(#{input}[@name='_.valueString'])[last()]").set(value)
    end

    def add_label_expression_axis(value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       find(:xpath, "//li/a[text()='Label expression']").click
       sleep 0.1 # wait until axis appear
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.LabelExpAxis']//td/input";
       find(:xpath, "(#{input}[@name='_.name'])[last()]").set(value)
    end

    def add_slaves_axis(value)
       ensure_config_page
       #puts find(:xpath, "//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']")
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']//td/input";
       if !(page.has_xpath?("//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']"))
           puts (page.has_xpath?(:xpath, "//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']"))
          find(:xpath, "//button[text()='Add axis']").click
          find(:xpath, "//li/a[text()='Slaves']").click
          sleep 0.1 # wait until axis appear
       end
       if !(find(:xpath, "(#{input}[@name='values' and @json='#{value}'])").visible?)
          puts "not visible"
          find(:xpath, "//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel1']//a").click
          find(:xpath, "//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel2']//a").click
       end
       puts ("visible")
       find(:xpath, "(#{input}[@name='values' and @json='#{value}'])").set(true)
       puts(find(:xpath, "//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']"))
    end

    def add_jdk_axis(value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       if !(page.has_xpath?(:xpath, "//li/a[text()='JDK']"))
          find(:xpath, "//button[text()='Add axis']").click
          find(:xpath, "//li/a[text()='JDK']").click
          sleep 0.1 # wait until axis appear
       end
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.JDKAxis']//td/input";
       find(:xpath, "(#{input}[@name='values' and @json='#{value}'])[last()]").set(true)
    end

    def combination_filter(filter)
       ensure_config_page
       find(:xpath, "//input[@name='hasCombinationFilter']").set(true)
       find(:xpath, "//input[@name='combinationFilter']").set(filter)
    end

    def run_configurations_sequentially
       ensure_config_page
       find(:xpath, "//input[@name='_.runSequentially']").set(true)
    end

    def touchstone_builds_first(filter, result)
       ensure_config_page
       find(:xpath, "//input[@name='_.hasTouchStoneCombinationFilter']").set(true)
       find(:xpath, "//input[@name='_.touchStoneCombinationFilter']").set(filter)
       find(:xpath, "//select[@name='touchStoneResultCondition']/option[@value='#{result}']").click
    end

    def matrix_configurations
      visit(job_url + "/ajaxMatrix")
      if !(page.has_xpath?("//div[@id='matrix']//a"))
        return Array.new(0)
      end
      paths = page.all(:xpath,"//div[@id='matrix']//a")
      index = 0
      configurations = Array.new(paths.length)
      while index < paths.length do
        name = paths[index].[]("href").split(job_url)[1].delete("/") #remove / from the path to gain name of configuration
        configurations[index] = Jenkins::MatrixConfiguration.new(@base_url, name, self)
        index += 1
      end
      return configurations
    end

    def self.create_matrix(base_url, name)
      visit("#{@base_url}/newJob")

      fill_in "name", :with => name
      find(:xpath, "//input[starts-with(@value, 'hudson.matrix.MatrixProject')]").set(true)
      click_button "OK"

      self.new(base_url, name)
    end
  end
end
