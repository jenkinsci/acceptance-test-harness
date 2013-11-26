require 'jenkins/pagearea'

module Jenkins
  class Scm
    include Jenkins::PageArea

    def self.add(job, title)
      xpath = "//label[contains(text(),'#{title}')]/input[@name='scm']"
      element = find(:xpath, xpath)
      element.set(true)

      type = get title
      return type.new(job, element[:path])
    end

    @@types = Hash.new

    # Register SCM type
    def self.register(title)
      raise "#{title} already registered" if @@types.has_key? title

      @@types[title] = self
    end

    # Get type by title
    def self.get(title)
      return @@types[title] if @@types.has_key? title

      raise "Unknown #{self.name.split('::').last} type #{title}. #{@@types.keys}"
    end
  end
end
