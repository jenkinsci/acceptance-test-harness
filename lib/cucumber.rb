# Monkey patching Cucumber
module Cucumber
  module Ast
    # A tag attached to a Feature or a Scenario
    #
    # In 'asdf(gh, jk)' the 'asdf' part is the name and 'gh' and 'jk' are its values
    class Tag

      attr_reader :name, :values

      # Parse tag literal
      def self.parse(tag)
        m = tag.name.match /^@(\w+)\(([^\)]+)\)$/
        if m.nil?
          return m
        end

        return Tag.new(m[1], m[2].split(/\s*,\s*/))
      end

      def initialize(name, values)
        @name = name
        @values = values
      end
    end

    class Scenario

      def skip_not_applicable(version)
        return false if version.nil?

        return false if feature.applicable_for?(version) && applicable_for?(version)
        skip!
        return true
      end

      def applicable_for?(version)
        Ast::applicable_for?(version, @tags.tags)
      end

      def skip!
        @steps.each{|step_invocation| step_invocation.skip_invoke!}
      end
    end

    class Feature
      def applicable_for?(version)
        Ast::applicable_for?(version, @tags.tags)
      end
    end

    def self.applicable_for?(version, tags)
      since_tag = find_tag('since', tags)

      if since_tag.nil?
        return true
      end

      required_version = Gem::Version.new(since_tag.values[0])
      return version > required_version
    end

    # Find tag by name
    def self.find_tag(name, tags)
      for tagString in tags
        tag = Cucumber::Ast::Tag.parse(tagString)
          if !tag.nil? && tag.name == name
            return tag
          end
        end

      return nil
    end
  end
end
