module Jenkins
  class MultipleScms < Scm

    register 'Multiple SCMs'

    def initialize(*args)
      super(*args)
      @last = 0
    end

    def add(title)
      control('hetero-list-add[scmList]').click

      find(:xpath, "//a[text() = '#{title}']").click

      new_path = path 'scmList'
      if @last != 0
        new_path += "[#{@last}]"
      end

      @last += 1

      type = Jenkins::Scm.get title
      return type.new(self, new_path)
    end
  end
end
