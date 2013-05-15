module Jenkins
  class SubversionScm < Scm

    register 'Subversion'

    def url(url)
      find(:path, path('locations/remote')).set(url)
    end

    def local_dir(dir)
      find(:path, path('locations/local')).set(dir)
    end
  end
end
