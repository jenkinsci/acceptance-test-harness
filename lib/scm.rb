require File.dirname(__FILE__) + "/pagearea.rb"

module Jenkins
  class Scm
    include Jenkins::PageArea

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
