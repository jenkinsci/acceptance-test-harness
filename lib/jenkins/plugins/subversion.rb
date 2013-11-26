module Jenkins
  class SubversionScm < Scm

    register 'Subversion'

    def url(url)
      control('locations/remote').set(url)
    end

    def local_dir(dir)
      control('locations/local').set(dir)
    end
  end
end
