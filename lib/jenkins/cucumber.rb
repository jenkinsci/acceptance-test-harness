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

      def skip_not_applicable(runner=nil)
        should_run = if runner.nil?
          Jenkins::ScenarioSkipper.should_run? self
        else
          Jenkins::ScenarioSkipper.should_run_against? self, runner
        end

        skip! if !should_run

        return !should_run
      end

      def skip!
        @steps.each{|step_invocation| step_invocation.skip_invoke!}
      end

      def tag(name)
        for tagString in tags
        tag = Cucumber::Ast::Tag.parse(tagString)
          if !tag.nil? && tag.name == name
            return tag
          end
        end

      return nil
      end

      def tags
        @tags.tags + feature.tags
      end
    end

    class Feature

      def tags
        @tags.tags
      end
    end
  end
end
